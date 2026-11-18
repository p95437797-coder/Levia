'use strict';
const schemas = require('../validators/authValidators');
const auth = require('../services/authService');
const asyncHandler = require('../utils/asyncHandler');

const ok = (res, data, status) => res.status(status || 200).json({ success: true, data });
const device = (req) => String(req.headers['user-agent'] || '').slice(0, 200);

exports.register = asyncHandler(async (req, res) => {
  const input = schemas.register.parse(req.body);
  ok(res, await auth.register(input, device(req)), 201);
});

exports.login = asyncHandler(async (req, res) => {
  const input = schemas.login.parse(req.body);
  ok(res, await auth.login(input, device(req)));
});

exports.refresh = asyncHandler(async (req, res) => {
  const { refreshToken } = schemas.refresh.parse(req.body);
  ok(res, await auth.refresh(refreshToken, device(req)));
});

exports.logout = asyncHandler(async (req, res) => {
  const { refreshToken } = schemas.refresh.parse(req.body);
  await auth.logout(refreshToken);
  ok(res, { loggedOut: true });
});

exports.logoutAll = asyncHandler(async (req, res) => {
  await auth.logoutAll(req.user.id);
  ok(res, { loggedOut: true });
});

exports.changePassword = asyncHandler(async (req, res) => {
  const { currentPassword, newPassword } = schemas.changePassword.parse(req.body);
  await auth.changePassword(req.user.id, currentPassword, newPassword);
  ok(res, { changed: true });
});

exports.forgotPassword = asyncHandler(async (req, res) => {
  const { email } = schemas.requestReset.parse(req.body);
  await auth.requestPasswordReset(email);
  ok(res, { message: 'If that email exists, a reset message has been sent.' });
});

exports.resetPassword = asyncHandler(async (req, res) => {
  const { token, newPassword } = schemas.resetPassword.parse(req.body);
  await auth.resetPassword(token, newPassword);
  ok(res, { reset: true });
});
