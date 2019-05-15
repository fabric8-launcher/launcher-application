export interface KeycloakConfig {
  clientId: string;
  realm: string;
  url: string;
}

export interface OpenshiftConfig {
  gitProvider: 'gitea' | 'github';
  gitea?: {
    clientId: string;
    secret: string;
    url: string;
    redirectUri: string;
    validateTokenUri: string;
  };
  github?: {
    clientId: string;
    secret: string;
    validateTokenUri: string;
  };
  openshift: {
    clientId: string;
    url: string;
    validateTokenUri: string;
    responseType?: string;
  };
}

export interface Authorizations {
  [headerName: string]: string;
}

export interface User {
  authorizationsByProvider: { [provider: string]: Authorizations | undefined };
  accountLink: { [provider: string]: string; };
  userName: string;
  userPreferredName: string;
  sessionState: string;
}

export type OptionalUser = User | undefined;
