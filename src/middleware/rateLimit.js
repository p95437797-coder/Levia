'use strict';
const rateLimit = require('express-rate-limit');

const json = (code, message) => ({ success: false, error: { code, message } });

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 30,
  standardHeaders: true,
  legacyHeaders: false,
  message: json('RATE_LIMITED', 'Too many attempts. Try again later.')
});

const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  limit: 240,
  standardHeaders: true,
  legacyHeaders: false,
  message: json('RATE_LIMITED', 'Too many requests. Slow down.')
});

module.exports = { authLimiter, apiLimiter };
