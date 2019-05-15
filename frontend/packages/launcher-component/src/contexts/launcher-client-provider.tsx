import React, { useState } from 'react';
import { LauncherClientContext } from './launcher-client-context';
import { cachedLauncherClient, checkNotNull, LauncherClient, mockLauncherClient } from '@launcher/client';
import { AuthorizationsManager, AuthorizationManagerContext } from './authorization-context';

interface LauncherDepsProvider {
  children: React.ReactNode;
  client?: LauncherClient;
  authorizationsManager?: AuthorizationsManager;
  creatorUrl?: string;
  launcherUrl?: string;
}

function buildLauncherClient(props: LauncherDepsProvider) {
  if (props.client) {
    return props.client;
  }
  let client: LauncherClient;
  if (!!props.creatorUrl || !!props.launcherUrl) {
    checkNotNull(props.launcherUrl, 'launcherUrl');
    checkNotNull(props.creatorUrl, 'creatorUrl');
    client = cachedLauncherClient({ creatorUrl: props.creatorUrl!, launcherURL: props.launcherUrl! });
  } else {
    client = mockLauncherClient();
  }
  return client;
}

function buildAuthorizationManager(props: LauncherDepsProvider): AuthorizationsManager {
  if (props.authorizationsManager) {
    return props.authorizationsManager;
  }
  return {
    getAuthorizations: () => Promise.resolve({}),
    generateAuthorizationLink: (provider?: string) => `http://mock-authorization/${provider}`
  };
}

export function LauncherDepsProvider(props: LauncherDepsProvider) {
  const [client] = useState<LauncherClient>(buildLauncherClient(props));
  const [authorizationManager] = useState<AuthorizationsManager>(buildAuthorizationManager(props));
  client.authorizationsProvider = authorizationManager.getAuthorizations;

  return (
    <LauncherClientContext.Provider value={client}>
      <AuthorizationManagerContext.Provider value={authorizationManager}>
        {props.children}
      </AuthorizationManagerContext.Provider>
    </LauncherClientContext.Provider>
  );
}
