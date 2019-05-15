import { AuthenticationApi } from '../authentication-api';
import { OptionalUser, User, Authorizations } from '../types';

const mockUser: User = {
  userName: 'Anonymous',
  userPreferredName: 'Anonymous',
  authorizationsByProvider: { mock: { Authorization: `Bearer eyJhbGciOiJIUzI1NiJ9.e30.ZRrHA1JJJW8opsbCGfG_HACGpVUMN_a9IV7pAx_Zmeo` }},
  sessionState: 'sessionState',
  accountLink: {},
};

export default class MockAuthenticationApi implements AuthenticationApi {
  private onUserChangeListener?: (user: OptionalUser) => void = undefined;
  private _user: OptionalUser;

  public setOnUserChangeListener(listener: (user: OptionalUser) => void) {
    this.onUserChangeListener = listener;
  }

  public init = (): Promise<OptionalUser> => {
    if (JSON.parse(sessionStorage.getItem('mock-auth') || 'false')) {
      this.login();
    }
    this.triggerUserChange();
    return Promise.resolve(this._user);
  };

  public async getAuthorizations(provider: string): Promise<Authorizations | undefined> {
    if (!this._user) {
      return;
    }
    return this._user.authorizationsByProvider['mock'];
  }

  public generateAuthorizationLink = (provider?: string, redirect?: string): string => {
    return `https://authorize/${provider}`;
  };

  public login = (): void => {
    this._user = mockUser;
    sessionStorage.setItem('mock-auth', JSON.stringify(true));
    this.triggerUserChange();
  };

  public logout = (): void => {
    this._user = undefined;
    sessionStorage.setItem('mock-auth', JSON.stringify(false));
    this.triggerUserChange();
  };

  public getAccountManagementLink = (): string | undefined => {
    return 'https://account-management';
  };

  public refreshToken = (force?: boolean): Promise<OptionalUser> => {
    this.triggerUserChange();
    return Promise.resolve(this._user);
  };

  public get user() {
    return this._user;
  }

  public get enabled(): boolean {
    return true;
  }

  private triggerUserChange() {
    if (this.onUserChangeListener) {
      this.onUserChangeListener(this._user);
    }
  }
}
