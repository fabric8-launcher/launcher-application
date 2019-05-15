import { OptionalUser, Authorizations } from './types';

export interface AuthenticationApi {
  readonly user: OptionalUser;
  readonly enabled: boolean;
  init(): Promise<OptionalUser>;
  login(): void;
  logout(): void;
  getAccountManagementLink(): string | undefined;
  refreshToken(force?: boolean): Promise<OptionalUser>;
  getAuthorizations(provider: string): Promise<Authorizations | undefined>;
  generateAuthorizationLink(provider?: string, redirect?: string): string;
  setOnUserChangeListener(listener: (user: OptionalUser) => void): void;
}
