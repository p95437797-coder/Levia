'use strict';
const app = require('./app');
const env = require('./config/env');

const server = app.listen(env.port, () => {
  console.log('LEVIA API listening on port ' + env.port + ' (' + env.nodeEnv + ')');
});

function shutdown() {
  server.close(() => process.exit(0));
  setTimeout(() => process.exit(1), 10000).unref();
}
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
