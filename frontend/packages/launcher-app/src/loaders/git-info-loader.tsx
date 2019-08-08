import React from 'react';

import { useLauncherClient } from '../contexts/launcher-client-context';
import { DataLoader } from '@launcher/component';
import { GitInfo, AuthorizationError } from '../client/types';
import { LauncherClient } from '../client/launcher.client';

export function GitInfoLoader(props: { children: (obj: GitInfo) => any }) {
  const client = useLauncherClient();
  return (
    <DataLoader loader={() => gitInfoLoader(client)}>
      {props.children}
    </DataLoader>
  );
}

export const gitInfoLoader = async (client: LauncherClient) => {
  try {
    return await client.gitInfo();
  } catch (error) {
    if (error instanceof AuthorizationError) {
      return Promise.resolve({} as GitInfo);
    }
    throw error;
  }
};
