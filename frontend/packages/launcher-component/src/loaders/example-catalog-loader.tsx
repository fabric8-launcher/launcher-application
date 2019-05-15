import React from 'react';

import { DataLoader } from '../core/data-loader/data-loader';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { AnyExample, constructModel, filter, ExampleMission } from 'launcher-client';

export function ExamplesLoaderWithFilter(props: { query: { missionId?: string, runtimeId?: string }, children: (obj: AnyExample) => any }) {
  const client = useLauncherClient();
  const itemsLoader = () => client.exampleCatalog().then(catalog => {
    const query = {
      mission: {
        id: props.query.missionId,
        name: '',
        runtime: {
          id: props.query.runtimeId,
          name: '',
          icon: ''
        }
      }
    };
    const anyExamples = filter(query, catalog);
    return anyExamples[0];
  });
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}

export function ExamplesLoader(props: { children: (missions: ExampleMission[]) => any }) {
  const client = useLauncherClient();
  const itemsLoader = async () => {
    return constructModel(await client.exampleCatalog());
  };
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}
