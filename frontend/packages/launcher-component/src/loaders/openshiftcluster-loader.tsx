import React from 'react';
import { OpenShiftCluster } from 'launcher-client';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { DataLoader } from '../core/data-loader/data-loader';
import * as _ from 'lodash';

export function OpenshiftClustersLoader(props: {children: (obj: OpenShiftCluster[]) => any }) {
  const client = useLauncherClient();
  const itemsLoader = () => client.ocClusters().then(clusters => {
    return _.orderBy(clusters, ['connected', 'id'], ['desc', 'asc']);
  });
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}

export function OpenshiftClusterLoader(props: {clusterId: string, children: (obj?: OpenShiftCluster) => any }) {
  const client = useLauncherClient();
  const itemsLoader = async () => {
    const clusters = await client.ocClusters();
    return clusters.find(c => c.id === props.clusterId);
  };
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}
