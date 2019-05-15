import { checkNotNull } from 'launcher-client';
import { OpenshiftConfig, KeycloakConfig } from '../auth/types';

function getEnv(env: string | undefined, name: string): string | undefined {
  const globalConfig = (window as any).GLOBAL_CONFIG;
  if (globalConfig && globalConfig[name] && globalConfig[name].length > 0) {
    return globalConfig[name];
  }
  if (env && env.length === 0) {
    return undefined;
  }
  return env;
}

function requireEnv(env: string | undefined, name: string): string {
  return checkNotNull(getEnv(env, name), `process.env.${name}`);
}

function getAuthMode(keycloakUrl?: string, openshiftOAuthUrl?: string) {
  const authMode = getEnv(process.env.REACT_APP_AUTHENTICATION, 'authMode');
  if (authMode) {
    return authMode;
  }
  if (keycloakUrl) {
    return 'keycloak';
  }
  if (openshiftOAuthUrl) {
    return 'oauth-openshift'
  }
  return 'no';
}

function getAuthConfig(authMode: string): KeycloakConfig | OpenshiftConfig | undefined {
  switch (authMode) {
    case 'keycloak':
      return {
        clientId: requireEnv(process.env.REACT_APP_KEYCLOAK_CLIENT_ID, 'keycloakClientId'),
        realm: requireEnv(process.env.REACT_APP_KEYCLOAK_REALM, 'keycloakRealm'),
        url: requireEnv(process.env.REACT_APP_KEYCLOAK_URL, 'keycloakUrl'),
      } as KeycloakConfig;
    case 'oauth-openshift':
      const base: OpenshiftConfig = {
        openshift: {
          clientId: requireEnv(process.env.REACT_APP_OAUTH_OPENSHIFT_CLIENT_ID, 'openshiftOAuthClientId'),
          url: requireEnv(process.env.REACT_APP_OAUTH_OPENSHIFT_URL, 'openshiftOAuthUrl'),
          validateTokenUri: `${requireEnv(process.env.REACT_APP_LAUNCHER_API_URL, 'launcherApiUrl')}/services/openshift/user`,
        },
        gitProvider: requireEnv(process.env.REACT_APP_GIT_PROVIDER, 'gitProvider') === 'github' ? 'github' : 'gitea',
      };
      if (base.gitProvider === 'github') {
        base.github = {
          clientId: requireEnv(process.env.REACT_APP_OAUTH_GITHUB_CLIENT_ID, 'githubOAuthClientId'),
          secret: requireEnv(process.env.REACT_APP_OAUTH_GITHUB_SECRET, 'githubOAuthSecret'),
          validateTokenUri: getEnv(process.env.REACT_APP_OAUTH_GITHUB_VALIDATE_URI, 'githubOAuthValidateUri') || '/launch/github/access_token',
        };
      }
      if (base.gitProvider === 'gitea') {
        base.gitea = {
          clientId: requireEnv(process.env.REACT_APP_OAUTH_GITEA_CLIENT_ID, 'giteaOAuthClientId'),
          secret: requireEnv(process.env.REACT_APP_OAUTH_GITEA_SECRET, 'giteaOAuthSecret'),
          url: requireEnv(process.env.REACT_APP_OAUTH_GITEA_URL, 'giteaOAuthUrl'),
          redirectUri: requireEnv(process.env.REACT_APP_OAUTH_GITEA_REDIRECT_URL, 'giteaOAuthRedirectUrl'),
          validateTokenUri: requireEnv(process.env.REACT_APP_OAUTH_GITEA_VALIDATE_URI, 'giteaOAuthValidateUri'),
        };
      }
      return base;
    case 'mock':
    case 'no':
      return undefined;
    default:
      throw new Error(`${authMode} is not supported.`);
  }
}

export const publicUrl = process.env.PUBLIC_URL && `${process.env.PUBLIC_URL}/`;

export const keycloakUrl = getEnv(process.env.REACT_APP_KEYCLOAK_URL, 'keycloakUrl');
export const openshiftOAuthUrl = getEnv(process.env.REACT_APP_OAUTH_OPENSHIFT_URL, 'openshiftOAuthUrl');
export const authMode = getAuthMode(keycloakUrl, openshiftOAuthUrl)

export const authConfig = getAuthConfig(authMode);

const launcherClientApiMode = process.env.REACT_APP_CLIENT !== 'mock';

export const creatorApiUrl =
  getEnv(launcherClientApiMode ? process.env.REACT_APP_CREATOR_API_URL : undefined, 'creatorApiUrl');

export const launcherApiUrl =
  getEnv(launcherClientApiMode ? process.env.REACT_APP_LAUNCHER_API_URL : undefined, 'launcherApiUrl');

export const sentryDsn =
  getEnv(process.env.REACT_APP_SENTRY_DSN, 'sentryDsn');
