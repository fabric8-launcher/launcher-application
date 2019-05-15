import { __RouterContext, RouteComponentProps } from 'react-router';
import * as H from 'history';
import { createLocation } from 'history';
import { BaseSyntheticEvent, useContext } from 'react';
import queryString from 'query-string';

export interface BaseRouter {
  location: H.Location;
  history: H.History;
}

export function useRouter<TParams = {}>() {
  return useContext(__RouterContext) as RouteComponentProps<TParams>;
}

export const createRouterHref = (router: BaseRouter, to: string) => {
  return router.history.createHref(createLocation(to, undefined, undefined, router.location));
};

export const goToWithRouter = (router: BaseRouter, to: string) => {
  router.history.push(to);
};

export const createRouterLink = (router: BaseRouter, to: string) => {
  return {
    href: createRouterHref(router, to),
    onClick: (e?: BaseSyntheticEvent) => {
      if (e) {
        e.preventDefault();
      }
      goToWithRouter(router, to);
    }
  };
};

export function getRequestedRoute(router: BaseRouter) {
  const search = router.location.search;
  if (!search) {
    return;
  }
  const requestedRoute = (queryString.parse(search).request as string);
  if(requestedRoute) {
    return requestedRoute;
  }
  return;
}
