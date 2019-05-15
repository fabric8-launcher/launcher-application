import { Alert } from '@patternfly/react-core';
import * as React from 'react';
import { isMockMode } from '../config/appConfig';
import { HttpApiContext } from '../HttpApiContext';
import { DatabaseCapability, DatabaseCapabilityApiContext, DatabaseCapabilityProps } from './database/DatabaseCapability';
import { mockDatabaseCapabilityApi, newHttpDatabaseCapabilityApi } from './database/DatabaseCapabilityApi';
import { HealthChecksApiContext, HealthChecksCapability, HealthChecksCapabilityProps } from './healthchecks/HealthChecksCapability';
import { mockHealthChecksCapabilityApi, newHttpHealthChecksCapabilityApi } from './healthchecks/HealthChecksCapabilityApi';
import { RestCapability, RestCapabilityApiContext, RestCapabilityProps } from './rest/RestCapability';
import { mockRestCapabilityApi, newHttpRestCapabilityApi } from './rest/RestCapabilityApi';

interface CapabilityProps {
  module?: string;
  [propId: string]: any;
}

export function Capability(props: CapabilityProps) {
  const httpApi = React.useContext(HttpApiContext);
  
  switch (props.module) {
    case 'database':
      return (
        <DatabaseCapabilityApiContext.Provider value={isMockMode ? mockDatabaseCapabilityApi : newHttpDatabaseCapabilityApi(httpApi)} >
          <DatabaseCapability {...props as DatabaseCapabilityProps} />
        </DatabaseCapabilityApiContext.Provider>
      );
    case 'rest':
      return (
        <RestCapabilityApiContext.Provider value={isMockMode ? mockRestCapabilityApi: newHttpRestCapabilityApi(httpApi)} >
          <RestCapability {...props as RestCapabilityProps} />
        </RestCapabilityApiContext.Provider>
      );
    case 'healthchecks':
      return (
        <HealthChecksApiContext.Provider value={isMockMode ? mockHealthChecksCapabilityApi: newHttpHealthChecksCapabilityApi(httpApi)} >
          <HealthChecksCapability {...props as HealthChecksCapabilityProps}/>
        </HealthChecksApiContext.Provider>
      );
    default:
      return (
        <Alert
          variant="warning"
          title={`'${props.module}' capability doesn't include any example yet.`}
          style={{margin: '10px 0'}}
        />
      );
  }
}
