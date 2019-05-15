import { PropertyValue } from '@launcher/client';
import React from 'react';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { DataLoader } from '../core/data-loader/data-loader';

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