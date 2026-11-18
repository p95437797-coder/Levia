'use strict';
const { ZodError } = require('zod');

function notFound(req, res) {
  res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'Route not found' } });
}

// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  if (err instanceof ZodError) {
    return res.status(400).json({
      success: false,
      error: {
        code: 'VALIDATION_ERROR',
        message: err.issues[0] ? err.issues[0].message : 'Invalid input',
        details: err.issues.map((i) => ({ field: i.path.join('.'), message: i.message }))
      }
    });
  }
  if (err.isAppError) {
    return res.status(err.status).json({ success: false, error: { code: err.code, message: err.message } });
  }
  if (err.type === 'entity.parse.failed') {
    return res.status(400).json({ success: false, error: { code: 'BAD_JSON', message: 'Malformed JSON body' } });
  }
  console.error('Unhandled error:', err);
  res.status(500).json({ success: false, error: { code: 'INTERNAL', message: 'Something went wrong' } });
}

module.exports = { notFound, errorHandler };
