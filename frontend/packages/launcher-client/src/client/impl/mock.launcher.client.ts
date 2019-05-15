/* test-code */
import { defaultAuthorizationsProvider, LauncherClient } from '../launcher.client';
import {
  AnalyzeResult, AnyExample,
  Capability,
  Catalog,
  DownloadAppPayload,
  DownloadAppResult,
  Enums,
  ExistsResult,
  GitInfo,
  GitProvider,
  GitRepositoryExistsPayload,
  LaunchAppPayload,
  LaunchAppResult,
  OpenShiftCluster,
  PropertyValue,
  StatusListener,
} from '../types';

import capabilities from '../data-examples/mock-capabilities.json';
import enums from '../data-examples/mock-enums.json';
import clusters from '../data-examples/mock-clusters.json';
import gitUser from '../data-examples/mock-git-user.json';
import exampleCatalog from '../data-examples/mock-example-catalog.json';
import analyzeResult from '../data-examples/mock-import-analyze.json';
import gitProviders from '../data-examples/mock-git-providers.json';
import quarkusExtensions from '../data-examples/mock-quarkus-extensions.json';
import { filter } from '../..';
import { waitForTick } from '../helpers/mock-helpers';

const progressDef = {
  success: [
    {
      statusMessage: 'GITHUB_CREATE',
      data: {
        location: 'https://github.com/fabric8-launcher/launcher-backend'
      }
    },
    {statusMessage: 'GITHUB_PUSHED'},
    {
      statusMessage: 'OPENSHIFT_CREATE',
      data: {
        location: 'https://console.starter-us-east-2.openshift.com/console/projects'
      }
    },
    {
      statusMessage: 'OPENSHIFT_PIPELINE', data: {
        routes: {
          'welcome': 'http://welcome-gullible-rake.7e14.starter-us-west-2.openshiftapps.com/',
          'meaty-spade': 'http://meaty-spade-meaty-spade.192.168.42.21.nip.io'
        }
      }
    },
    {statusMessage: 'GITHUB_WEBHOOK'},
  ],
};

export default class MockLauncherClient implements LauncherClient {

  public authorizationsProvider = defaultAuthorizationsProvider;

  public currentPayload?: DownloadAppPayload | LaunchAppPayload;

  constructor() {
  }

  public async exampleCatalog(): Promise<Catalog> {
    await waitForTick('exampleCatalog()', 300);
    return exampleCatalog as Catalog;
  }

  public async findExampleApps(query: AnyExample): Promise<AnyExample[]> {
    return filter(query, await this.exampleCatalog());
  }

  public async capabilities(): Promise<Capability[]> {
    await waitForTick('capabilities()', 300);
    return capabilities;
  }

  public async enum(id: string): Promise<PropertyValue[]> {
    await waitForTick(`enum(${id})`, 300);
    if(id === 'quarkus-extensions') {
      return quarkusExtensions;
    }
    return enums[id];
  }

  public async enums(): Promise<Enums> {
    await waitForTick('enums', 300);
    return enums;
  }

  public async importAnalyze(gitImportUrl: string): Promise<AnalyzeResult> {
    await waitForTick(`importAnalyze(${gitImportUrl})`, 300);
    return analyzeResult;
  }

  public async download(payload: DownloadAppPayload): Promise<DownloadAppResult> {
    this.currentPayload = payload;
    await waitForTick(`download(${JSON.stringify(payload)})`, 500);
    return {
      downloadLink: `http://mock/result.zip`
    };
  }

  public async launch(payload: LaunchAppPayload): Promise<LaunchAppResult> {
    this.currentPayload = payload;
    await waitForTick(`launch(${JSON.stringify(payload)})`, 1000);
    console.info(`calling launch with projectile: ${JSON.stringify(payload)}`);
    return {
      id: `success`,
      events: [
        {name: 'GITHUB_CREATE', message: 'Creating your new GitHub repository'},
        {name: 'GITHUB_PUSHED', message: 'Pushing your customized Booster code into the repo'},
        {name: 'OPENSHIFT_CREATE', message: 'Creating your project on OpenShift Online'},
        {name: 'OPENSHIFT_PIPELINE', message: 'Setting up your build pipeline'},
        {name: 'GITHUB_WEBHOOK', message: 'Configuring to trigger builds on Git pushes'}
      ]
    };
  }

  public follow(id: string, events: Array<{ name: string }>, listener: StatusListener) {
    const progress = progressDef[id];
    if (!progress) {
      throw new Error(`invalid id ${id}`);
    }
    let i = 0;
    const interval = setInterval(value => {
      if (i < progress.length) {
        listener.onMessage(progress[i++]);
      } else {
        clearInterval(interval);
        listener.onComplete();
      }
    }, 1000);
  }

  public async gitProviders(): Promise<GitProvider[]> {
    await waitForTick('gitProviders()', 300);
    return gitProviders as GitProvider[];
  }

  public async gitRepositoryExists(payload: GitRepositoryExistsPayload): Promise<ExistsResult> {
    await waitForTick(`gitRepositoryExists(${JSON.stringify(payload)})`, 300);
    return {exists: false};
  }

  public async gitInfo(): Promise<GitInfo> {
    await waitForTick('gitInfo()', 300);
    return gitUser;
  }

  public async ocClusters(): Promise<OpenShiftCluster[]> {
    await waitForTick('ocClusters()', 300);
    return clusters.map(c => ({
      ...c.cluster, connected: c.connected
    }));
  }

  public async ocExistsProject(projectName: string): Promise<ExistsResult> {
    await waitForTick('ocExistsProject()', 300);
    return {exists: projectName === 'my-project'};
  }
}
/* end-test-code */
