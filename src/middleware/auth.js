'use strict';
const jwt = require('jsonwebtoken');
const env = require('../config/env');
const { pool } = require('../config/db');
const AppError = require('../utils/AppError');
const asyncHandler = require('../utils/asyncHandler');

/** Requires a valid access token; also checks account status and token_version (session revocation). */
const authenticate = asyncHandler(async (req, res, next) => {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) throw new AppError(401, 'Authentication required', 'UNAUTHENTICATED');

  let payload;
  try {
    payload = jwt.verify(token, env.jwtAccessSecret, { algorithms: ['HS256'] });
  } catch (e) {
    throw new AppError(401, 'Invalid or expired token', 'TOKEN_INVALID');
  }

  const { rows } = await pool.query(
    'SELECT id, username, role, status, token_version FROM users WHERE id = $1', [payload.sub]);
  const u = rows[0];
  if (!u || u.token_version !== payload.tv) throw new AppError(401, 'Session expired', 'TOKEN_INVALID');
  if (u.status !== 'active') throw new AppError(403, 'Account is not active', 'ACCOUNT_INACTIVE');

  req.user = { id: u.id, username: u.username, role: u.role };
  next();
});

/** Authorization: allow only the listed roles. Use after authenticate. */
const requireRole = (...roles) => (req, res, next) => {
  if (!req.user || !roles.includes(req.user.role)) {
    return next(new AppError(403, 'Forbidden', 'FORBIDDEN'));
  }
  next();
};

module.exports = { authenticate, requireRole };
