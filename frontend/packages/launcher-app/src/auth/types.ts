export interface KeycloakConfig {
  clientId: string;
  realm: string;
  url: string;
  gitProvider: 'gitea' | 'github' | 'gitlab' | 'bitbucket';
}

export interface OpenshiftConfig {
  loadGitProvider: () => Promise<GitProviderConfig>,
  openshift: {
    clientId: string;
    url: string;
    validateTokenUri: string;
    responseType?: string;
  };
}

export interface GitProviderConfig {
  gitProvider: 'gitea' | 'github' | 'gitlab' | 'bitbucket';
  gitea?: {
    clientId: string;
    url: string;
    redirectUri?: string;
    validateTokenUri: string;
  };
  github?: {
    clientId: string;
    url: string;
    redirectUri?: string;
    validateTokenUri: string;
  };
  gitlab?: {
    clientId: string;
    url: string;
    redirectUri?: string;
    validateTokenUri: string;
  };
  bitbucket?: {
    clientId: string;
    url: string;
    redirectUri?: string;
    validateTokenUri: string;
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
