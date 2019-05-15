import * as React from 'react';
import { generate } from 'project-name-generator';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';

import { buildDownloadImportAppPayload, buildLaunchImportAppPayload } from './launcher-client-adapters';
import { SrcRepositoryHub } from '../hubs/src-repository-hub';
import { LaunchFlow, useAutoSetCluster, NAME_REGEX } from './launch-flow';
import { DeploymentHub } from '../hubs/deployment-hub';
import { ImportApp } from './types';
import { ProjectNameInput } from '../core/project-name-input/project-name-input';

const DEFAULT_IMPORT_APP = {
  srcRepository: {},
  deployment: {},
};

function getFlowStatus(app: ImportApp) {
  if (!NAME_REGEX.test(app.name)) {
    return {
      hint: 'You should enter a valid name for your application',
      isReadyForDownload: false,
      isReadyForLaunch: false
    };
  }
  if (!SrcRepositoryHub.checkCompletion(app.srcRepository)) {
    return {
      hint: 'You should configure the source repository.',
      isReadyForDownload: false,
      isReadyForLaunch: false,
    };
  }
  if (!DeploymentHub.checkCompletion(app.deployment)) {
    return {
      hint: 'If you wish to Launch your application, you should configure OpenShift Deployment.',
      isReadyForDownload: true,
      isReadyForLaunch: false,
    };
  }
  return {
    hint: 'Your application is ready to launch!',
    isReadyForDownload: true,
    isReadyForLaunch: true,
  };
}

export function ImportExistingFlow(props: { appName?: string; onCancel?: () => void }) {
  const defaultAppName = props.appName || generate().dashed;
  const defaultImportApp = {...DEFAULT_IMPORT_APP, name: defaultAppName};
  const [app, setApp, clear] = useSessionStorageWithObject<ImportApp>('import-existing-app', defaultImportApp);
  const onCancel = () => {
    clear();
    props.onCancel!();
  };
  const showDeploymentForm = useAutoSetCluster(setApp);

  const flowStatus = getFlowStatus(app);

  const items = [
    {
      id: SrcRepositoryHub.id,
      title: SrcRepositoryHub.title,
      overview: {
        component: ({edit}) => (
          <SrcRepositoryHub.Overview value={app.srcRepository} onClick={edit}/>
        ),
        width: 'half',
      },
      form: {
        component: ({close}) => (
          <SrcRepositoryHub.Form
            initialValue={app.srcRepository}
            onSave={(srcRepository) => {
              setApp(prev => ({...prev, srcRepository}));
              close();
            }}
            onCancel={close}
          />
        ),
      }
    },
    {
      id: DeploymentHub.id,
      title: DeploymentHub.title,
      overview: {
        component: ({edit}) => (
          <DeploymentHub.Overview value={app.deployment} onClick={edit}/>
        ),
        width: 'half',
      },
      form: showDeploymentForm && {
        component: ({close}) => (
          <DeploymentHub.Form
            initialValue={app.deployment}
            onSave={(deployment) => {
              setApp(prev => ({...prev, deployment}));
              close();
            }}
            onCancel={close}
          />
        ),
      }
    }
  ];

  return (
    <LaunchFlow
      title={(
        <ProjectNameInput
          prefix="Import an Existing Application as:"
          value={app.name}
          onChange={value => setApp(prev => ({...prev, name: value}))}
        />
      )}

      items={items}
      {...flowStatus}
      buildLaunchAppPayload={() => {
        clear();
        return buildLaunchImportAppPayload(app);
      }}
      buildDownloadAppPayload={() => {
        clear();
        return buildDownloadImportAppPayload(app);
      }}
      onCancel={onCancel}
    />
  );

}
