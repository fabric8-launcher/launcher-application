import {
  AnalyzeResult, AnyExample,
  AuthorizationsProvider,
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
} from './types';

export const defaultAuthorizationsProvider = async () => undefined;

export interface LauncherClient {
  authorizationsProvider: AuthorizationsProvider;

  exampleCatalog(): Promise<Catalog>;

  findExampleApps(query: AnyExample):
    Promise<AnyExample[]>;

  enum(id: string): Promise<PropertyValue[]>;

  enums(): Promise<Enums>;

  capabilities(): Promise<Capability[]>;

  importAnalyze(gitImportUrl: string): Promise<AnalyzeResult>;

  download(payload: DownloadAppPayload): Promise<DownloadAppResult>;

  launch(payload: LaunchAppPayload): Promise<LaunchAppResult>;

  follow(id: string, events: Array<{ name: string }>, listener: StatusListener);

  ocExistsProject(projectName: string): Promise<ExistsResult>;

  ocClusters(): Promise<OpenShiftCluster[]>;

  gitProviders(): Promise<GitProvider[]>;

  gitRepositoryExists(payload: GitRepositoryExistsPayload): Promise<ExistsResult>;

  gitInfo(): Promise<GitInfo>;

}
