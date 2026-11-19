'use strict';
const express = require('express');
const c = require('../controllers/authController');
const { authenticate } = require('../middleware/auth');
const { authLimiter } = require('../middleware/rateLimit');

const router = express.Router();

router.post('/register', authLimiter, c.register);
router.post('/login', authLimiter, c.login);
router.post('/refresh', authLimiter, c.refresh);
router.post('/logout', c.logout);
router.post('/forgot-password', authLimiter, c.forgotPassword);
router.post('/reset-password', authLimiter, c.resetPassword);

router.post('/logout-all', authenticate, c.logoutAll);
router.post('/change-password', authenticate, c.changePassword);

module.exports = router;
