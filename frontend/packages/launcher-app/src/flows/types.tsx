import { BackendFormValue } from '../hubs/backend-hub';
import { FrontendFormValue } from '../hubs/frontend-hub';
import { DestRepositoryFormValue } from '../hubs/dest-repository-hub';
import { DeploymentFormValue } from '../hubs/deployment-hub';
import { ExampleFormValue } from '../hubs/example-hub';
import { SrcRepositoryFormValue } from '../hubs/src-repository-hub';
import { WelcomeFormValue } from '../hubs/welcome-app-hub';

export interface NewApp {
  name: string;
  backend: BackendFormValue;
  frontend: FrontendFormValue;
  destRepository: DestRepositoryFormValue;
  deployment: DeploymentFormValue;
  welcomeApp: WelcomeFormValue;
}

export interface ExampleApp {
  name: string;
  example: ExampleFormValue;
  destRepository: DestRepositoryFormValue;
  deployment: DeploymentFormValue;
}

export interface ImportApp {
  name: string;
  srcRepository: SrcRepositoryFormValue;
  deployment: DeploymentFormValue;
}
