'use strict';
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const env = require('../config/env');
const { pool, withTransaction } = require('../config/db');
const AppError = require('../utils/AppError');
const { sendResetEmail } = require('../utils/mailer');

const BCRYPT_COST = 12;
const RESET_MINUTES = 30;
// Compared against when the email is unknown, so timing does not reveal account existence.
const DUMMY_HASH = bcrypt.hashSync('levia-dummy-password', BCRYPT_COST);

const sha256 = (s) => crypto.createHash('sha256').update(s).digest('hex');
const randomToken = (bytes) => crypto.randomBytes(bytes).toString('hex');

function publicUser(u) {
  return {
    id: u.id,
    email: u.email,
    username: u.username,
    displayName: u.display_name,
    bio: u.bio,
    avatarUrl: u.avatar_url,
    isPrivate: u.is_private,
    isVerifiedCreator: u.is_verified_creator,
    role: u.role,
    createdAt: u.created_at
  };
}

function signAccessToken(user) {
  return jwt.sign({ sub: user.id, tv: user.token_version }, env.jwtAccessSecret, {
    algorithm: 'HS256',
    expiresIn: '15m'
  });
}

/** Creates a refresh token row (in the given family) and returns the raw token. */
async function createRefreshToken(client, userId, familyId, deviceInfo) {
  const raw = randomToken(48);
  const expires = new Date(Date.now() + env.refreshTokenDays * 86400000);
  const { rows } = await client.query(
    `INSERT INTO refresh_tokens (user_id, token_hash, family_id, device_info, expires_at)
     VALUES ($1, $2, $3, $4, $5) RETURNING id`,
    [userId, sha256(raw), familyId, deviceInfo || null, expires]);
  return { raw, id: rows[0].id };
}

async function issueTokens(client, user, deviceInfo) {
  const familyId = crypto.randomUUID();
  const rt = await createRefreshToken(client, user.id, familyId, deviceInfo);
  return { accessToken: signAccessToken(user), refreshToken: rt.raw, expiresIn: 900 };
}

async function register({ email, username, displayName, password }, deviceInfo) {
  const hash = await bcrypt.hash(password, BCRYPT_COST);
  try {
    return await withTransaction(async (client) => {
      const { rows } = await client.query(
        `INSERT INTO users (email, username, display_name, password_hash)
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [email, username, displayName, hash]);
      const user = rows[0];
      await client.query('INSERT INTO user_settings (user_id) VALUES ($1)', [user.id]);
      const tokens = await issueTokens(client, user, deviceInfo);
      return { user: publicUser(user), tokens };
    });
  } catch (err) {
    if (err.code === '23505') throw new AppError(409, 'Email or username already taken', 'CONFLICT');
    throw err;
  }
}

async function login({ email, password }, deviceInfo) {
  const { rows } = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
  const user = rows[0];
  const ok = await bcrypt.compare(password, user ? user.password_hash : DUMMY_HASH);
  if (!user || !ok) throw new AppError(401, 'Invalid email or password', 'INVALID_CREDENTIALS');
  if (user.status === 'deactivated') {
    // Logging in reactivates a self-deactivated account.
    await pool.query("UPDATE users SET status = 'active' WHERE id = $1", [user.id]);
    user.status = 'active';
  }
  if (user.status !== 'active') throw new AppError(403, 'This account is ' + user.status, 'ACCOUNT_INACTIVE');

  const tokens = await withTransaction(async (client) => {
    await client.query('UPDATE users SET last_seen_at = NOW() WHERE id = $1', [user.id]);
    return issueTokens(client, user, deviceInfo);
  });
  return { user: publicUser(user), tokens };
}

async function refresh(refreshToken, deviceInfo) {
  const hash = sha256(refreshToken);
  // Reuse detection must persist even though we then throw, so it runs outside the rotation transaction.
  const reuse = await pool.query(
    'SELECT family_id FROM refresh_tokens WHERE token_hash = $1 AND revoked_at IS NOT NULL', [hash]);
  if (reuse.rows[0]) {
    await pool.query('UPDATE refresh_tokens SET revoked_at = NOW() WHERE family_id = $1 AND revoked_at IS NULL',
      [reuse.rows[0].family_id]);
    throw new AppError(401, 'Session expired. Please log in again.', 'TOKEN_REUSED');
  }

  return withTransaction(async (client) => {
    const { rows } = await client.query(
      `SELECT rt.id, rt.user_id, rt.family_id, rt.expires_at, rt.revoked_at, rt.device_info
       FROM refresh_tokens rt WHERE rt.token_hash = $1 FOR UPDATE`, [hash]);
    const row = rows[0];
    if (!row || row.revoked_at || new Date(row.expires_at) < new Date()) {
      throw new AppError(401, 'Session expired. Please log in again.', 'TOKEN_INVALID');
    }
    const u = await client.query('SELECT * FROM users WHERE id = $1', [row.user_id]);
    const user = u.rows[0];
    if (!user || user.status !== 'active') throw new AppError(403, 'Account is not active', 'ACCOUNT_INACTIVE');

    const next = await createRefreshToken(client, user.id, row.family_id, deviceInfo || row.device_info);
    await client.query('UPDATE refresh_tokens SET revoked_at = NOW(), replaced_by = $2 WHERE id = $1',
      [row.id, next.id]);
    return { accessToken: signAccessToken(user), refreshToken: next.raw, expiresIn: 900 };
  });
}

async function logout(refreshToken) {
  await pool.query('UPDATE refresh_tokens SET revoked_at = NOW() WHERE token_hash = $1 AND revoked_at IS NULL',
    [sha256(refreshToken)]);
}

async function revokeEverywhere(client, userId) {
  await client.query('UPDATE users SET token_version = token_version + 1 WHERE id = $1', [userId]);
  await client.query('UPDATE refresh_tokens SET revoked_at = NOW() WHERE user_id = $1 AND revoked_at IS NULL',
    [userId]);
}

async function logoutAll(userId) {
  await withTransaction((client) => revokeEverywhere(client, userId));
}

async function changePassword(userId, currentPassword, newPassword) {
  const { rows } = await pool.query('SELECT password_hash FROM users WHERE id = $1', [userId]);
  if (!rows[0] || !(await bcrypt.compare(currentPassword, rows[0].password_hash))) {
    throw new AppError(401, 'Current password is incorrect', 'INVALID_CREDENTIALS');
  }
  const hash = await bcrypt.hash(newPassword, BCRYPT_COST);
  await withTransaction(async (client) => {
    await client.query('UPDATE users SET password_hash = $2 WHERE id = $1', [userId, hash]);
    await revokeEverywhere(client, userId);
  });
}

/** Always resolves the same way so callers cannot tell whether an email exists. */
async function requestPasswordReset(email) {
  const { rows } = await pool.query("SELECT id FROM users WHERE email = $1 AND status <> 'deleted'", [email]);
  if (!rows[0]) return;
  const token = randomToken(32);
  await pool.query(
    `INSERT INTO password_reset_tokens (user_id, token_hash, expires_at)
     VALUES ($1, $2, NOW() + ($3 || ' minutes')::interval)`,
    [rows[0].id, sha256(token), String(RESET_MINUTES)]);
  try {
    await sendResetEmail(email, token);
  } catch (err) {
    console.error('Failed to send reset email:', err.message);
  }
}

async function resetPassword(token, newPassword) {
  const hash = await bcrypt.hash(newPassword, BCRYPT_COST);
  await withTransaction(async (client) => {
    const { rows } = await client.query(
      `SELECT id, user_id FROM password_reset_tokens
       WHERE token_hash = $1 AND used_at IS NULL AND expires_at > NOW() FOR UPDATE`, [sha256(token)]);
    if (!rows[0]) throw new AppError(400, 'Reset link is invalid or expired', 'RESET_INVALID');
    await client.query('UPDATE password_reset_tokens SET used_at = NOW() WHERE id = $1', [rows[0].id]);
    await client.query('UPDATE users SET password_hash = $2 WHERE id = $1', [rows[0].user_id, hash]);
    await revokeEverywhere(client, rows[0].user_id);
  });
}

module.exports = {
  register, login, refresh, logout, logoutAll, changePassword, requestPasswordReset, resetPassword,
  revokeEverywhere
};
