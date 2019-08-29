import { OpenshiftAuthenticationApi } from './openshift-authentication-api';

export class KeycloakAuthenticationApi extends OpenshiftAuthenticationApi {

  public generateAuthorizationLink(provider?: string, redirect?: string): string {
    if (provider !== 'github' && provider !== 'gitea') {
      return provider || '';
    }
    return super.generateAuthorizationLink(provider, redirect);
  };

}
