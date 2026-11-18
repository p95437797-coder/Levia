'use strict';
const express = require('express');
const { authenticate } = require('../middleware/auth');
const { pool } = require('../config/db');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.use('/auth', require('./authRoutes'));

// Current user (used by the app to confirm a session is valid)
router.get('/me', authenticate, asyncHandler(async (req, res) => {
  const { rows } = await pool.query(
    `SELECT id, email, username, display_name, bio, avatar_url, is_private,
            is_verified_creator, role, created_at FROM users WHERE id = $1`, [req.user.id]);
  const u = rows[0];
  res.json({ success: true, data: { user: {
    id: u.id, email: u.email, username: u.username, displayName: u.display_name, bio: u.bio,
    avatarUrl: u.avatar_url, isPrivate: u.is_private, isVerifiedCreator: u.is_verified_creator,
    role: u.role, createdAt: u.created_at } } });
}));

module.exports = router;
