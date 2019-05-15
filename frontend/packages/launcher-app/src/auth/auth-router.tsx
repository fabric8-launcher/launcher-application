import * as React from 'react';
import { BrowserRouter, Redirect, Route, RouteComponentProps, Switch } from 'react-router-dom';
import { useAuthenticationApi } from './auth-context';
import { useRouter } from '../router/use-router';

type RouterComponent = React.ComponentType<RouteComponentProps<any>> | React.ComponentType<any>;

interface AuthRouterProps {
  loginPage: RouterComponent;
  homePage?: RouterComponent;
  basename?: string;
  children?: React.ReactNode;
}

function GuestRoutes(props: { loginPage: RouterComponent, basename?: string; }) {
  const path = props.basename ? location.pathname.replace(props.basename, '/') : location.pathname;
  return (
    <Switch>
      <Route path="/login" exact component={props.loginPage} />
      <Redirect to={{ pathname: '/login', search: `?request=${path}` }} />
    </Switch>
  );
}

export function AuthRouter(props: AuthRouterProps) {
  const authApi = useAuthenticationApi();
  if (!authApi.user && authApi.enabled) {
    return (
      <BrowserRouter basename={props.basename}>
        <GuestRoutes {...props} />
      </BrowserRouter>
    );
  }
  if (props.children) {
    return (<React.Fragment>{props.children}</React.Fragment>);
  }
  return (
    <BrowserRouter basename={props.basename}>
      <Switch>
        <Route path="/" exact component={props.homePage} />
        <Redirect to="/" />
      </Switch>
    </BrowserRouter>
  );
}
