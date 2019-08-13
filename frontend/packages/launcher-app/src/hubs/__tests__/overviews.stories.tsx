import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { LauncherDepsProvider } from '../..';
import { FrontendHub } from '../frontend-hub';
import { ExampleHub } from '../example-hub';
import { SrcRepositoryHub } from '../src-repository-hub';
import { BackendHub } from '../backend-hub';
import { readOnlyCapabilities } from '../../loaders/new-app-capabilities-loader';
import { DestRepositoryHub } from '../dest-repository-hub';
import { DeploymentHub } from '../deployment-hub';

storiesOf('Overviews', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('BackendOverview: empty', () => {
    return (
      <BackendHub.Overview value={{ capabilitiesPickerValue: { capabilities: readOnlyCapabilities } }} onClick={action('overview')} />
    );
  })
  .add('BackendOverview: selected', () => {

    const value = {
      runtimePickerValue: {
        runtimeId: 'vertx'
      },
      capabilitiesPickerValue: {
        capabilities: [
          { id: 'welcome', selected: true },
          { id: 'database', selected: true }
        ]
      },
    };

    return (
      <BackendHub.Overview value={value} onClick={action('overview')} />
    );
  })
  .add('FrontendOverview: empty', () => {
    return (
      <FrontendHub.Overview value={{}} onClick={action('overview')} />
    );
  })
  .add('FrontendOverview: selected', () => {

    const value = {
      runtimePickerValue: {
        runtimeId: 'react'
      }
    };

    return (
      <FrontendHub.Overview value={value} onClick={action('overview')} />
    );
  })
  .add('ExampleOverview: empty', () => {
    return (
      <ExampleHub.Overview value={{}} onClick={action('overview')} />
    );
  })
  .add('ExampleOverview: selected', () => {

    const value = {
      examplePickerValue: {
        missionId: 'crud',
        runtimeId: 'vert.x',
        versionId: 'community'
      }
    };

    return (
      <ExampleHub.Overview value={value} onClick={action('overview')} />
    );
  })
  .add('ImportOverview: empty', () => {
    return (
      <SrcRepositoryHub.Overview value={{}} onClick={action('overview')} />
    );
  })
  .add('ImportOverview: selected', () => {

    const value = {
      gitUrlPickerValue: { url: 'https://github.com/fabric8-launcher/launcher-frontend' },
      buildImagePickerValue: { image: 'Java Code Builder' }
    };

    return (
      <SrcRepositoryHub.Overview value={value} onClick={action('overview')} />
    );
  })
  .add('Destination Overview: empty', () => {
    return (
      <DestRepositoryHub.Overview value={{}} onClick={action('overview')} />
    );
  })
  .add('Destination Overview: selected', () => {

    const value = {
      userRepositoryPickerValue: { name: 'monkey', org: 'coding' },
      isProviderAuthorized: true
    };

    return (
      <DestRepositoryHub.Overview value={value} onClick={action('overview')} />
    );
  })
  .add('DeploymentHub Overview: empty', () => {
    return (
      <DeploymentHub.Overview value={{}} onClick={action('overview')} />
    );
  })
  .add('DeploymentHub Overview: selected', () => {

    const value = {
      clusterPickerValue: { clusterId: 'starter-ca-central-1', clusterType: 'starter'},
    };

    return (
      <DeploymentHub.Overview value={value} onClick={action('overview')} />
    );
  });

