'use strict';
const nodemailer = require('nodemailer');
const env = require('../config/env');

let transporter = null;
if (env.smtp.host) {
  transporter = nodemailer.createTransport({
    host: env.smtp.host,
    port: env.smtp.port,
    secure: env.smtp.port === 465,
    auth: env.smtp.user ? { user: env.smtp.user, pass: env.smtp.pass } : undefined
  });
}

/**
 * Sends the password reset email.
 * Without SMTP configured, the link is only printed in development (never in production).
 */
async function sendResetEmail(email, token) {
  const link = env.appBaseUrl + '/reset-password?token=' + token;
  if (!transporter) {
    if (!env.isProd) console.log('[mailer] SMTP not configured. Reset link for ' + email + ': ' + link);
    else console.warn('[mailer] SMTP not configured; reset email NOT sent.');
    return;
  }
  await transporter.sendMail({
    from: env.smtp.from,
    to: email,
    subject: 'Reset your LEVIA password',
    text: 'Use this link to reset your password (valid 30 minutes):\n' + link +
      '\n\nReset code: ' + token + '\n\nIf you did not request this, ignore this email.'
  });
}

module.exports = { sendResetEmail };
