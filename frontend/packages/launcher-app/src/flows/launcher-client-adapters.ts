import { ExampleApp, ImportApp, NewApp } from './types';
import { convertToObject } from '../loaders/buildimage-loader';

export function buildLaunchNewAppPayload(app: NewApp) {
  return {
    ...buildDownloadNewAppPayload(app),
    gitRepository: app.destRepository.userRepositoryPickerValue!.name!,
    gitOrganization: app.destRepository.userRepositoryPickerValue!.org || '',
    ...app.deployment.clusterPickerValue,
    projectName: app.name,
  };
}

export function buildDownloadNewAppPayload(app: NewApp) {
  let parts: any[] = [];

  if (app.frontend.runtimePickerValue) {
    parts.push({
      category: 'frontend',
      shared: {
        runtime: {
          name: app.frontend.runtimePickerValue!.runtimeId,
          version: app.frontend.runtimePickerValue!.versionId
        }
      },
      capabilities: [{
        module: 'web-app'
      }],
    });
  }

  if (app.backend.runtimePickerValue) {
    parts.push({
      category: 'backend',
      shared: {
        runtime: {
          name: app.backend.runtimePickerValue!.runtimeId,
          version: app.backend.runtimePickerValue!.versionId
        }
      },
      capabilities: app.backend.capabilitiesPickerValue!.capabilities!
        .filter(c => c.selected)
        .map(c => ({module: c.id, props: c.data}))
    });
  }

  if (parts.length > 1) {
    parts = [
      ...parts.map(p => ({...p, subFolderName: p.category})),
      {category: 'support', subFolderName: 'support', capabilities: [{module: 'welcome'}]}
    ];
  } else {
    parts[0].capabilities.push({module: 'welcome'});
  }

  return {
    project: {
      application: app.name,
      parts,
    }
  };
}

export function buildLaunchExampleAppPayload(app: ExampleApp) {
  const parts: any[] = [];

  parts.push({
    category: 'example',
    shared: {
      mission: {id: app.example.examplePickerValue!.missionId!},
      runtime: {name: app.example.examplePickerValue!.runtimeId!, version: app.example.examplePickerValue!.versionId!}
    }
  });

  return {
    ...buildDownloadExampleAppPayload(app),
    gitRepository: app.destRepository.userRepositoryPickerValue!.name!,
    gitOrganization: app.destRepository.userRepositoryPickerValue!.org || '',
    ...app.deployment.clusterPickerValue,
    projectName: app.name!,
  };
}

export function buildDownloadExampleAppPayload(app: ExampleApp) {
  const parts: any[] = [];

  parts.push({
    category: 'example',
    shared: {
      mission: {id: app.example.examplePickerValue!.missionId!},
      runtime: {name: app.example.examplePickerValue!.runtimeId!, version: app.example.examplePickerValue!.versionId!}
    }
  });

  return {
    project: {
      application: app.name,
      parts,
    },
  };
}

export function buildLaunchImportAppPayload(app: ImportApp) {
  const downloadImportAppPayload = buildDownloadImportAppPayload(app);
  return {
    ...downloadImportAppPayload,
    ...app.deployment.clusterPickerValue,
    projectName: downloadImportAppPayload.project.application,
  };
}

export function buildDownloadImportAppPayload(app: ImportApp) {
  const parts: any[] = [];

  parts.push({
    category: 'import',
    shared: {},
    capabilities: [
      {
        module: 'import',
        props: {
          gitImportUrl: app.srcRepository.gitUrlPickerValue!.url!,
          builderImage: app.srcRepository.buildImagePickerValue!.image!,
          env: convertToObject(app.srcRepository.envPickerValue!.envVars!)
        }
      }
    ]
  });

  return {
    project: {
      application: app.name,
      parts,
    },
  };
}
