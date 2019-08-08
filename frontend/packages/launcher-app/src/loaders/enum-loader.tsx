import React from 'react';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { PropertyValue } from '../client/types';
import { DataLoader } from '@launcher/component';

export function EnumLoader(props: { name: string, children: (items: PropertyValue[]) => any }) {
  const client = useLauncherClient();
  const loader = async () => {
    return await client.enum(props.name);
  };
  return (
    <DataLoader loader={loader}>
      {props.children}
    </DataLoader>
  );
}