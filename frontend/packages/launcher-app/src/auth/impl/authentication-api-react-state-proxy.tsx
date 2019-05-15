import { AuthenticationApi } from '../authentication-api';
import { OptionalUser, Authorizations } from '../types';

export default class AuthenticationApiReactStateProxy implements AuthenticationApi {

  constructor(private readonly authApi: AuthenticationApi, private setIsLoggedIn: (isLoggedIn: boolean) => void) {
  }

  public async init(): Promise<OptionalUser> {
    this.authApi.setOnUserChangeListener((changed) => this.setIsLoggedIn(!!changed));
    return await this.authApi.init();
  }

  public getAuthorizations = async (provider: string): Promise<Authorizations | undefined> => {
    return this.authApi.getAuthorizations(provider);
  }

  public generateAuthorizationLink = (provider?: string, redirect?: string): string => {
    return this.authApi.generateAuthorizationLink(provider, redirect);
  };

  public login = (): void => {
    this.authApi.login();
  };

  public logout = (): void => {
    this.authApi.logout();
  };

  public getAccountManagementLink = () => {
    return this.authApi.getAccountManagementLink();
  };

  public refreshToken = async (force?: boolean): Promise<OptionalUser> => {
    return await this.authApi.refreshToken(force);
  };

  public get user() {
    return this.authApi.user;
  }

  public get enabled(): boolean {
    return this.authApi.enabled;
  }

  public setOnUserChangeListener(listener: (user: OptionalUser) => void) {
    throw new Error('setOnUserChangeListener should not be called on the proxy');
  }

}
