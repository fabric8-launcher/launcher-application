const baseConfig = require('./jest.config.base');

baseConfig.roots = [
  './packages/keycloak-react/src',
  './packages/launcher-client/src',
  './packages/launcher-component/src',
  './packages/launcher-app/src',
  './packages/launcher-welcome-app/src',
]

module.exports = baseConfig;
