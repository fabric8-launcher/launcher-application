import { RouteComponentProps } from 'react-router';
import { Context } from 'react';

declare module 'react-router' {
  export const __RouterContext: Context<RouteComponentProps<{}>>;
}