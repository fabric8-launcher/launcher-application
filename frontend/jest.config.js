const baseConfig = require('./jest.config.base');

baseConfig.roots = [
  './packages/launcher-app/src',
  './packages/launcher-welcome-app/src',
]

module.exports = baseConfig;
