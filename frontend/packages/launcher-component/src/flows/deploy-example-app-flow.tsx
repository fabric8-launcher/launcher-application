import * as React from 'react';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';
import { generate } from 'project-name-generator';
import { DestRepositoryHub } from '../hubs/dest-repository-hub';
import { buildDownloadExampleAppPayload, buildLaunchExampleAppPayload } from './launcher-client-adapters';
import { ExampleHub } from '../hubs/example-hub';
import { LaunchFlow, useAutoSetCluster, useAutoSetDestRepository, NAME_REGEX } from './launch-flow';
import { DeploymentHub } from '../hubs/deployment-hub';
import { ExampleApp } from './types';
import { ProjectNameInput } from '../core/project-name-input/project-name-input';

const DEFAULT_EXAMPLE_APP = {
  name: 'example-app',
  example: {},
  destRepository: {},
  deployment: {},
};

function getFlowStatus(app: ExampleApp) {
  if (!NAME_REGEX.test(app.name)) {
    return {
      hint: 'You should enter a valid name for your application',
      isReadyForDownload: false,
      isReadyForLaunch: false
    };
  }
  if (!ExampleHub.checkCompletion(app.example)) {
    return {
      hint: 'You should select an example application.',
      isReadyForDownload: false,
      isReadyForLaunch: false,
    };
  }
  if (app.example.examplePickerValue!.downloadOnly) {
    return {
      hint: `This example is using some specifics that can't be launched by our system for the moment.
       Please download the ZIP file and follow the instructions in the README.md file to deploy in a local cluster.`,
      isReadyForDownload: true,
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
  if (!DestRepositoryHub.checkCompletion(app.destRepository)) {
    return {
      hint: 'If you wish to Launch your application, you should configure the destination repository.',
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

export function DeployExampleAppFlow(props: { appName?: string; onCancel?: () => void }) {
  const defaultNewApp = { ...DEFAULT_EXAMPLE_APP, name: props.appName || generate().dashed };
  const [app, setApp, clear] = useSessionStorageWithObject<ExampleApp>('deploy-example-app', defaultNewApp);
  const autoSetCluster = useAutoSetCluster(setApp);
  const autoSetDestRepository = useAutoSetDestRepository(app.name, setApp);

  const onCancel = () => {
    clear();
    props.onCancel!();
  };

  const flowStatus = getFlowStatus(app);

  const items = [
    {
      id: ExampleHub.id,
      title: ExampleHub.title,
      overview: {
        component: ({ edit }) => (
          <ExampleHub.Overview value={app.example} onClick={edit} />
        ),
        width: 'third',
      },
      form: {
        component: ({ close }) => (
          <ExampleHub.Form
            initialValue={app.example}
            onSave={(example) => {
              setApp((prev) => ({ ...prev, example }));
              close();
            }}
            onCancel={close}
          />
        ),
      }
    },
    {
      id: DestRepositoryHub.id,
      title: DestRepositoryHub.title,
      loading: autoSetDestRepository.loading,
      overview: {
        component: ({ edit }) => (
          <DestRepositoryHub.Overview value={app.destRepository} onClick={edit} />
        ),
        width: 'third',
      },
      form: autoSetDestRepository.showForm && {
        component: ({ close }) => (
          <DestRepositoryHub.Form
            initialValue={app.destRepository}
            onSave={(srcLocation) => {
              setApp((prev) => ({ ...prev, destRepository: srcLocation }));
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
      loading: autoSetCluster.loading,
      overview: {
        component: ({ edit }) => (
          <DeploymentHub.Overview value={app.deployment} onClick={edit} />
        ),
        width: 'third',
      },
      form: autoSetCluster.showForm && {
        component: ({ close }) => (
          <DeploymentHub.Form
            initialValue={app.deployment}
            onSave={(deployment) => {
              setApp((prev) => ({ ...prev, deployment }));
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
          prefix="Create Example Application as:"
          value={app.name}
          onChange={value => setApp(prev => ({ ...prev, name: value }))}
        />
      )}
      items={items}
      {...flowStatus}
      buildLaunchAppPayload={() => {
        clear();
        return buildLaunchExampleAppPayload(app);
      }}
      buildDownloadAppPayload={() => {
        clear();
        return buildDownloadExampleAppPayload(app);
      }}
      onCancel={onCancel}
    />
  );

}
