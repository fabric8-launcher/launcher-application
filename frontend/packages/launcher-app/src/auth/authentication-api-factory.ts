import MockAuthenticationApi from './impl/mock-authentication-api';
import { ClusterAuthenticationApi } from './impl/cluster-authentication-api';
import NoAuthenticationApi from './impl/no-authentication-api';
import { AuthenticationApi } from './authentication-api';
import { OpenshiftAuthenticationApi } from './impl/openshift-authentication-api';
import { OpenshiftConfig } from './types';
import { checkNotNull } from '../client/helpers/preconditions';

export { AuthenticationApiContext, useAuthenticationApi, useAuthenticationApiStateProxy } from './auth-context';
export { AuthRouter } from './auth-router';

export function newMockAuthApi() { return new MockAuthenticationApi(); }
export function newKCAuthApi(config: OpenshiftConfig) { return new ClusterAuthenticationApi(config); }
export function newOpenshiftAuthApi(config: OpenshiftConfig) { return new OpenshiftAuthenticationApi(config); }
export function newNoAuthApi() { return new NoAuthenticationApi(); }

export function newAuthApi(authenticationMode?: string, config?: OpenshiftConfig): AuthenticationApi {
  switch (authenticationMode) {
    case 'no':
      return new NoAuthenticationApi();
    case 'mock':
      return new MockAuthenticationApi();
    case 'oauth-cluster':
      return new ClusterAuthenticationApi(checkNotNull(config as OpenshiftConfig, 'oauthConfig'));
    case 'oauth-openshift':
      return new OpenshiftAuthenticationApi(checkNotNull(config as OpenshiftConfig, 'openshiftConfig'));
    default:
      throw new Error(`Invalid authentication mode: ${authenticationMode}`);
  }
}
