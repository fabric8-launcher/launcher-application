import * as React from 'react';
import { useContext, useState } from 'react';
import { AuthenticationApi } from './authentication-api';
import NoAuthenticationApi from './impl/no-authentication-api';
import AuthenticationApiReactStateProxy from './impl/authentication-api-react-state-proxy';

export const AuthenticationApiContext = React.createContext<AuthenticationApi>(new NoAuthenticationApi());

export function useAuthenticationApiStateProxy(authApi: AuthenticationApi): AuthenticationApi {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  console.debug(`User isLoggedIn status: ${isLoggedIn}`);
  return new AuthenticationApiReactStateProxy(authApi, setIsLoggedIn);
}

export function useAuthenticationApi(): AuthenticationApi {
  return useContext(AuthenticationApiContext);
}
