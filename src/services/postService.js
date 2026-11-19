'use strict';
const { pool, withTransaction } = require('../config/db');
const AppError = require('../utils/AppError');

const POST_SELECT = `SELECT p.id, p.body, p.category, p.created_at AS "createdAt",
  p.like_count AS "likeCount", p.comment_count AS "commentCount",
  p.comments_enabled AS "commentsEnabled", p.is_pinned AS "isPinned",
  u.id AS "authorId", u.username, u.display_name AS "displayName",
  u.avatar_url AS "avatarUrl", u.is_verified_creator AS "isVerifiedCreator",
  EXISTS (SELECT 1 FROM likes l WHERE l.post_id = p.id AND l.user_id = $1) AS "likedByMe"
  FROM posts p JOIN users u ON u.id = p.author_id`;

function visibilitySql(viewerParam) {
  return `AND (u.is_private = FALSE OR p.author_id = ${viewerParam} OR EXISTS (
      SELECT 1 FROM follows vf WHERE vf.follower_id = ${viewerParam} AND vf.following_id = p.author_id))
    AND NOT EXISTS (SELECT 1 FROM blocks b WHERE
      (b.blocker_id = ${viewerParam} AND b.blocked_id = p.author_id) OR
      (b.blocker_id = p.author_id AND b.blocked_id = ${viewerParam}))`;
}

async function listPosts(viewerId, { limit, cursor, q, username }) {
  const params = [viewerId];
  let sql = `${POST_SELECT} WHERE p.deleted_at IS NULL AND p.is_draft = FALSE AND p.is_anonymous = FALSE AND u.status = 'active'`;
  sql += ` ${visibilitySql('$1')}`;
  if (q) { params.push(`%${q.replace(/[\\%_]/g, '\\$&')}%`); sql += ` AND p.body ILIKE $${params.length} ESCAPE '\\'`; }
  if (username) { params.push(username); sql += ` AND u.username = $${params.length}`; }
  if (cursor) { params.push(cursor); sql += ` AND p.created_at < $${params.length}::timestamptz`; }
  params.push(limit + 1);
  sql += ` ORDER BY p.created_at DESC LIMIT $${params.length}`;
  const result = await pool.query(sql, params);
  let items = result.rows;
  let nextCursor = null;
  if (items.length > limit) {
    items = items.slice(0, limit);
    nextCursor = new Date(items[items.length - 1].createdAt).toISOString();
  }
  items = items.map(item => ({ ...item, createdAt: new Date(item.createdAt).toISOString() }));
  return { items, nextCursor };
}

async function createPost(userId, fields) {
  const result = await pool.query(
    `INSERT INTO posts (author_id, body, category, comments_enabled)
     VALUES ($1, $2, $3, $4) RETURNING id`,
    [userId, fields.body, fields.category, fields.commentsEnabled]);
  const created = await pool.query(`${POST_SELECT} WHERE p.id = $2`, [userId, result.rows[0].id]);
  const row = created.rows[0];
  return row ? { ...row, createdAt: new Date(row.createdAt).toISOString() }
    : { id: result.rows[0].id, body: fields.body, category: fields.category };
}

async function ensureVisiblePost(client, viewerId, postId) {
  const result = await client.query(
    `SELECT p.id, p.author_id, p.comments_enabled, u.is_private FROM posts p
     JOIN users u ON u.id = p.author_id
     WHERE p.id = $2 AND p.deleted_at IS NULL AND p.is_draft = FALSE AND u.status = 'active'
       AND (u.is_private = FALSE OR p.author_id = $1 OR EXISTS
         (SELECT 1 FROM follows f WHERE f.follower_id = $1 AND f.following_id = p.author_id))
       AND NOT EXISTS (SELECT 1 FROM blocks b WHERE
         (b.blocker_id = $1 AND b.blocked_id = p.author_id) OR
         (b.blocker_id = p.author_id AND b.blocked_id = $1))`, [viewerId, postId]);
  if (!result.rowCount) throw new AppError(404, 'Post not found', 'POST_NOT_FOUND');
  return result.rows[0];
}

async function likePost(userId, postId) {
  return withTransaction(async client => {
    await ensureVisiblePost(client, userId, postId);
    await client.query('INSERT INTO likes (user_id, post_id) VALUES ($1, $2) ON CONFLICT DO NOTHING', [userId, postId]);
    const count = await client.query('UPDATE posts SET like_count = (SELECT COUNT(*)::int FROM likes WHERE post_id = $1) WHERE id = $1 RETURNING like_count', [postId]);
    return { liked: true, likeCount: count.rows[0].like_count };
  });
}

async function unlikePost(userId, postId) {
  return withTransaction(async client => {
    await ensureVisiblePost(client, userId, postId);
    await client.query('DELETE FROM likes WHERE user_id = $1 AND post_id = $2', [userId, postId]);
    const count = await client.query('UPDATE posts SET like_count = (SELECT COUNT(*)::int FROM likes WHERE post_id = $1) WHERE id = $1 RETURNING like_count', [postId]);
    return { liked: false, likeCount: count.rows[0].like_count };
  });
}

async function listComments(userId, postId, limit) {
  await ensureVisiblePost(pool, userId, postId);
  const result = await pool.query(
    `SELECT c.id, c.body, c.created_at AS "createdAt", c.like_count AS "likeCount",
            u.id AS "authorId", u.username, u.display_name AS "displayName", u.avatar_url AS "avatarUrl"
       FROM comments c JOIN users u ON u.id = c.author_id
      WHERE c.post_id = $1 AND c.deleted_at IS NULL
      ORDER BY c.created_at ASC LIMIT $2`, [postId, limit]);
  return { items: result.rows.map(r => ({ ...r, createdAt: new Date(r.createdAt).toISOString() })) };
}

async function addComment(userId, postId, body) {
  return withTransaction(async client => {
    const post = await ensureVisiblePost(client, userId, postId);
    if (!post.comments_enabled) throw new AppError(403, 'Comments are disabled for this post', 'COMMENTS_DISABLED');
    const result = await client.query(
      `INSERT INTO comments (post_id, author_id, body) VALUES ($1, $2, $3)
       RETURNING id, body, created_at AS "createdAt"`, [postId, userId, body]);
    const count = await client.query(
      'UPDATE posts SET comment_count = (SELECT COUNT(*)::int FROM comments WHERE post_id = $1 AND deleted_at IS NULL) WHERE id = $1 RETURNING comment_count', [postId]);
    const author = await client.query('SELECT username, display_name AS "displayName", avatar_url AS "avatarUrl" FROM users WHERE id = $1', [userId]);
    return { ...result.rows[0], ...author.rows[0], authorId: userId, likeCount: 0, commentCount: count.rows[0].comment_count };
  });
}

module.exports = { listPosts, createPost, likePost, unlikePost, listComments, addComment };
