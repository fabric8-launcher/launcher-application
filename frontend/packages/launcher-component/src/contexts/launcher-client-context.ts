import * as React from 'react';
import { useContext } from 'react';
import { checkNotNull, LauncherClient } from '@launcher/client';

export const LauncherClientContext = React.createContext<LauncherClient | undefined>(undefined);

export function useLauncherClient(): LauncherClient {
  const client = useContext(LauncherClientContext);
  return checkNotNull(client, 'launcher client must be defined in context');
}
