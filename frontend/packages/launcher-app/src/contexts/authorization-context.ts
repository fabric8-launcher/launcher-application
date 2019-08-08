import React, { useContext } from 'react';
import { Authorizations } from '../auth/types';
import { checkNotNull } from '../client/helpers/preconditions';

export interface AuthorizationsManager {
    getAuthorizations: (provider: string) => Promise<Authorizations | undefined>;
    generateAuthorizationLink(provider?: string, redirect?: string): string;
}

export const AuthorizationManagerContext = React.createContext<AuthorizationsManager | undefined>(undefined);

export function useAuthorizationManager(): AuthorizationsManager {
  const auth = useContext(AuthorizationManagerContext);
  return checkNotNull(auth, 'Authorization must be defined in context');
}
