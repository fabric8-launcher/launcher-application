import MockAuthenticationApi from './impl/mock-authentication-api';
import { KeycloakAuthenticationApi } from './impl/keycloak-authentication-api';
import NoAuthenticationApi from './impl/no-authentication-api';
import { AuthenticationApi } from './authentication-api';
import { checkNotNull } from '@launcher/client';
import { OpenshiftAuthenticationApi } from './impl/openshift-authentication-api';
import { KeycloakConfig, OpenshiftConfig } from './types';

export { AuthenticationApiContext, useAuthenticationApi, useAuthenticationApiStateProxy } from './auth-context';
export { AuthRouter } from './auth-router';

export function newMockAuthApi() { return new MockAuthenticationApi(); }
export function newKCAuthApi(config: KeycloakConfig) { return new KeycloakAuthenticationApi(config); }
export function newOpenshiftAuthApi(config: OpenshiftConfig) { return new OpenshiftAuthenticationApi(config); }
export function newNoAuthApi() { return new NoAuthenticationApi(); }

export function newAuthApi(authenticationMode?: string, config?: OpenshiftConfig|KeycloakConfig): AuthenticationApi {
  switch (authenticationMode) {
    case 'no':
      return new NoAuthenticationApi();
    case 'mock':
      return new MockAuthenticationApi();
    case 'keycloak':
      return new KeycloakAuthenticationApi(checkNotNull(config as KeycloakConfig, 'keycloakConfig'));
    case 'oauth-openshift':
      return new OpenshiftAuthenticationApi(checkNotNull(config as OpenshiftConfig, 'openshiftConfig'));
    default:
      throw new Error(`Invalid authentication mode: ${authenticationMode}`);
  }
}
