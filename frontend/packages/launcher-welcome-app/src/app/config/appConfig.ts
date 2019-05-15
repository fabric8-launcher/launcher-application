import { checkNotNull } from '../../shared/utils/Preconditions';
import mockAppDefinition from './mockAppConfig';
import { AppDefinition, adaptAppDefinition } from './AppDefinition';
import { getLocationAbsoluteUrl } from '../../shared/utils/Locations';

export interface AppConfig {
  definition?: AppDefinition;
  backendUrl: string;
  openshiftConsoleUrl?: string;
  sourceRepository?: {
    url: string;
    provider: string;
  };
}

export const isMockMode = checkNotNull(process.env.REACT_APP_MODE, 'process.env.REACT_APP_MODE') === 'mock';

export function getRouteLink(routeName: string) {
  return getLocationAbsoluteUrl().replace('welcome-', routeName + '-');
}

const appConfig: AppConfig = {
  backendUrl: '/backend'
};

interface InjectedConfig {
  openshiftConsoleUrl: string;
  encodedDefinition: string;
  sourceRepositoryUrl: string;
  sourceRepositoryProvider: string;
}

declare var INJECTED_CONFIG: InjectedConfig | undefined;

if (!isMockMode) {
  checkNotNull(INJECTED_CONFIG, 'INJECTED_CONFIG');
  try {
    appConfig.definition = adaptAppDefinition(JSON.parse(INJECTED_CONFIG!.encodedDefinition));
  } catch (e) {
    throw new Error('Error while parsing WelcomeApp config: ' + e.toString());
  }

  appConfig.openshiftConsoleUrl = INJECTED_CONFIG!.openshiftConsoleUrl;
  const sourceRepositoryUrl = INJECTED_CONFIG!.sourceRepositoryUrl;
  if (sourceRepositoryUrl) {
    appConfig.sourceRepository = {
      url: sourceRepositoryUrl,
      provider: INJECTED_CONFIG!.sourceRepositoryProvider!,
    };
  }
} else {
  appConfig.definition = adaptAppDefinition(mockAppDefinition);
  appConfig.openshiftConsoleUrl = 'http://consoleUrl.mock.io';
  appConfig.sourceRepository = {
    url: 'https://github.com/fabric8-launcher/launcher-creator-welcome-app.git',
    provider: 'GitHub',
  };
}

checkNotNull(appConfig.definition, 'appConfig.definition');

export default appConfig;
