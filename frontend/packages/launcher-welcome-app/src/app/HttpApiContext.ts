import appConfig from './config/appConfig';
import { AxiosHttpApi, HttpApi } from '../shared/utils/HttpApi';
import React from 'react';

export const HttpApiContext = React.createContext<HttpApi>(new AxiosHttpApi({ serviceUrl: appConfig.backendUrl }));