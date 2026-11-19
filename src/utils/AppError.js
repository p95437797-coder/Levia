'use strict';
class AppError extends Error {
  constructor(status, message, code) {
    super(message);
    this.status = status;
    this.code = code || 'ERROR';
    this.isAppError = true;
  }
}
module.exports = AppError;
