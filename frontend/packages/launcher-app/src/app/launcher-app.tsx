import '@patternfly/react-core/dist/styles/base.css';
import React, { useContext, useEffect, useState } from 'react';
import { Redirect, Route, Switch } from 'react-router';
import { BrowserRouter } from 'react-router-dom';
import { AuthenticationApiContext, useAuthenticationApiStateProxy } from '../auth/auth-context';
import { newAuthApi } from '../auth/authentication-api-factory';
import { createRouterLink, getRequestedRoute, useRouter } from '../router/use-router';
import { authConfig, authMode, creatorApiUrl, launcherApiUrl, publicUrl, trackerToken } from './config';
import './launcher-app.scss';
import { Layout } from './layout';
import { LoginPage } from './login-page';
import { LauncherMenu } from '../launcher/launcher';
import { CreateNewAppFlow } from '../flows/create-new-app-flow';
import { ImportExistingFlow } from '../flows/import-existing-flow';
import { DeployExampleAppFlow } from '../flows/deploy-example-app-flow';
import { DataLoader, AnalyticsContext, useAnalytics, Analytics, GoogleAnalytics } from '@launcher/component';
import { LauncherDepsProvider } from '../contexts/launcher-client-provider';

function Routes(props: {}) {
  const analytics = useContext(AnalyticsContext);
  const router = useRouter();
  const requestedRoute = getRequestedRoute(router) || sessionStorage.getItem('redirectUrl');

  if (requestedRoute) {
    sessionStorage.removeItem('redirectUrl');
    return <Redirect to={requestedRoute} />
  }

  const AnalyticsRoute = ({ ...rest }) => {
    analytics.pageview(router.location.pathname);
    return (<Route {...rest} />)
  };

  const Menu = () => {
    return (
      <LauncherMenu
        createNewApp={createRouterLink(router, '/flow/new-app')}
        createExampleApp={createRouterLink(router, '/flow/deploy-example-app')}
        importExistingApp={createRouterLink(router, '/flow/import-existing-app')}
      />
    );
  };
  const WithCancel = (cancelProps: { children: (onCancel: () => void) => any }) => {
    const rootLink = createRouterLink(router, '/home');
    return cancelProps.children(rootLink.onClick);
  };
  const CreateNewAppFlowRoute = () => (<WithCancel>{onCancel => <CreateNewAppFlow onCancel={onCancel} />}</WithCancel>);
  const ImportExistingFlowRoute = () => (<WithCancel>{onCancel => <ImportExistingFlow onCancel={onCancel} />}</WithCancel>);
  const DeployExampleAppFlowRoute = () => (<WithCancel>{onCancel => <DeployExampleAppFlow onCancel={onCancel} />}</WithCancel>);
  return (
    <Switch>
      <AnalyticsRoute path="/" exact component={LoginPage} />
      <AnalyticsRoute path="/home" exact component={Menu} />
      <AnalyticsRoute path="/flow/new-app" exact component={CreateNewAppFlowRoute} />
      <AnalyticsRoute path="/flow/import-existing-app" exact component={ImportExistingFlowRoute} />
      <AnalyticsRoute path="/flow/deploy-example-app" exact component={DeployExampleAppFlowRoute} />
      <AnalyticsRoute path="/flow/deploy-example-app" exact component={DeployExampleAppFlowRoute} />
      <Redirect to="/" />
    </Switch>
  );
}

function HomePage(props: {}) {
  return (
    <BrowserRouter basename={publicUrl}>
      <Layout>
        <div className="launcher-container">
          <Routes />
        </div>
      </Layout>
    </BrowserRouter>
  );
}

const authApi = newAuthApi(authMode, authConfig);

export function LauncherApp() {
  const [analytics, setAnalytics] = useState<Analytics>(useAnalytics());

  useEffect(() => {
    setAnalytics((prev) => {
      const newAnalytics = trackerToken ? new GoogleAnalytics(trackerToken) : prev;
      newAnalytics.init();
      return newAnalytics;
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const proxyAuthApi = useAuthenticationApiStateProxy(authApi);
  const authLoader = () => {
    return proxyAuthApi.init().catch(e => console.error(e));
  };
  return (
    <DataLoader loader={authLoader}>
      <AuthenticationApiContext.Provider value={proxyAuthApi}>
        <AnalyticsContext.Provider value={analytics}>
          <LauncherDepsProvider
            authorizationsManager={proxyAuthApi}
            creatorUrl={creatorApiUrl}
            launcherUrl={launcherApiUrl}
          >
            <HomePage />
          </LauncherDepsProvider>
        </AnalyticsContext.Provider>
      </AuthenticationApiContext.Provider>
    </DataLoader >
  );
}
