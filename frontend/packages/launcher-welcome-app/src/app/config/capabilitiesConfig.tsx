import { HeartbeatIcon, PlugIcon, DatabaseIcon } from '@patternfly/react-icons';
import * as React from 'react';

export interface CapabilityConfig {
  readonly name: string;
  readonly icon: React.ReactNode;
}

const capabilitiesConfig = {
  healthchecks: {
    module: 'healthchecks',
    name: 'Health Checks',
    icon: (<HeartbeatIcon className="with-text" />),
    requireDefinition: false,
  },
  rest: {
    module: 'rest',
    name: 'HTTP APIs',
    icon: (<PlugIcon className="with-text" />),
    requireDefinition: true,
  },
  database: {
    module: 'database',
    name: 'Relational Persistence',
    icon: (<DatabaseIcon className="with-text" />),
    requireDefinition: true,
  },
};

export default capabilitiesConfig;
