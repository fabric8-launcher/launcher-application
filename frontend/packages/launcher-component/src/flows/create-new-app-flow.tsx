import * as React from 'react';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';
import { generate } from 'project-name-generator';
import { BackendHub } from '../hubs/backend-hub';
import { FrontendHub, } from '../hubs/frontend-hub';
import { DestRepositoryHub } from '../hubs/dest-repository-hub';
import { LaunchFlow, useAutoSetCluster, useAutoSetDestRepository, NAME_REGEX } from './launch-flow';
import { buildDownloadNewAppPayload, buildLaunchNewAppPayload } from './launcher-client-adapters';
import { DeploymentHub } from '../hubs/deployment-hub';
import { readOnlyCapabilities } from '../loaders/new-app-capabilities-loader';
import { WelcomeAppHub } from '../hubs/welcome-app-hub';
import { NewApp } from './types';
import { ProjectNameInput } from '../core/project-name-input/project-name-input';

const DEFAULT_NEW_APP = {
  name: 'my-app',
  backend: { capabilitiesPickerValue: { capabilities: readOnlyCapabilities } },
  frontend: {},
  destRepository: {},
  deployment: {},
  welcomeApp: { selected: true }
};

function getFlowStatus(app: NewApp) {
  if (!NAME_REGEX.test(app.name)) {
    return {
      hint: 'You should enter a valid name for your application',
      isReadyForDownload: false,
      isReadyForLaunch: false
    };
  }
  if (!FrontendHub.checkCompletion(app.frontend) && !BackendHub.checkCompletion(app.backend)) {
    return {
      hint: 'You should configure a Frontend and/or a Backend for your application.',
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
  if (!DestRepositoryHub.checkCompletion(app.destRepository)) {
    return {
      hint: 'If you wish   to Launch your application, you should configure the destination repository.',
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

export function CreateNewAppFlow(props: { appName?: string; onCancel?: () => void }) {
  const defaultAppName = props.appName || generate().dashed;
  const defaultNewApp = { ...DEFAULT_NEW_APP, name: defaultAppName };
  const [app, setApp, clear] = useSessionStorageWithObject<NewApp>('new-app-flow', defaultNewApp);
  const autoSetCluster = useAutoSetCluster(setApp);
  const autoSetDestRepository = useAutoSetDestRepository(app.name, setApp);

  const onCancel = () => {
    clear();
    props.onCancel!();
  };

  const items = [
    {
      id: FrontendHub.id,
      title: FrontendHub.title,
      overview: {
        component: ({ edit }) => (
          <FrontendHub.Overview value={app.frontend} onClick={edit} />
        ),
        width: 'third',
      },
      form: {
        component: ({ close }) => (
          <FrontendHub.Form
            initialValue={app.frontend}
            onSave={(frontend) => {
              setApp((prev) => ({ ...prev, frontend }));
              close();
            }}
            onCancel={close}
          />
        ),
      }
    },
    {
      id: BackendHub.id,
      title: BackendHub.title,
      overview: {
        component: ({ edit }) => (
          <BackendHub.Overview value={app.backend} onClick={edit} />
        ),
        width: 'third',
      },
      form: {
        component: ({ close }) => (
          <BackendHub.Form
            initialValue={app.backend}
            onSave={(backend) => {
              setApp((prev) => ({ ...prev, backend }));
              close();
            }}
            onCancel={close}
          />
        ),
      }
    },
    {
      id: WelcomeAppHub.id,
      title: WelcomeAppHub.title,
      overview: {
        component: ({ edit }) => (
          <WelcomeAppHub.Overview value={app.welcomeApp} onClick={edit} />
        ),
        width: 'third',
      },
      form: {
        component: ({ close }) => (
          <WelcomeAppHub.Form
            initialValue={app.welcomeApp}
            onSave={(welcomeApp) => {
              setApp((prev) => ({ ...prev, welcomeApp }));
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
        width: 'half',
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
        width: 'half',
      },
      form: autoSetCluster.showForm && {
        component: ({ close }) => (
          <DeploymentHub.Form
            initialValue={app.deployment}
            onSave={(deployment) => {
              setApp(prev => ({ ...prev, deployment }));
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
          prefix="Create New Application as:"
          value={app.name}
          onChange={value => setApp(prev => ({ ...prev, name: value }))}
        />
      )}
      items={items}
      {...getFlowStatus(app)}
      buildLaunchAppPayload={() => {
        clear();
        return buildLaunchNewAppPayload(app);
      }}
      buildDownloadAppPayload={() => {
        clear();
        return buildDownloadNewAppPayload(app);
      }}
      onCancel={onCancel}
    />
  );
}
