'use strict';
const express = require('express');
const { authenticate, optionalAuthenticate } = require('../middleware/auth');
const c = require('../controllers/profileController');

const router = express.Router();

router.get('/profile/me', authenticate, c.me);
router.patch('/profile/me', authenticate, c.update);
router.get('/profile/settings', authenticate, c.getSettings);
router.patch('/profile/settings', authenticate, c.updateSettings);
// Order matters: /users/search must be registered before /users/:username.
router.get('/users/search', optionalAuthenticate, c.search);
router.get('/users/:username', optionalAuthenticate, c.byUsername);

module.exports = router;
