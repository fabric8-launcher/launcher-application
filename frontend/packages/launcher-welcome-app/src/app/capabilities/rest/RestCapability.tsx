import { Grid, GridItem, TextInput } from '@patternfly/react-core';
import * as React from 'react';
import { HttpRequest, RequestsConsole, useRequestsState } from '../../../shared/components/HttpRequest';
import { RequestTitle } from '../../../shared/components/RequestTitle';
import { SourceMappingLink } from '../../../shared/components/SourceMappingLink';
import CapabilityCard from '../../components/CapabilityCard';
import capabilitiesConfig from '../../config/capabilitiesConfig';
import { mockRestCapabilityApi, REST_GREETING_PATH } from './RestCapabilityApi';

export interface RestCapabilityProps {
  sourceRepository?: {
    url: string;
    provider: string;
  };
  sourceMapping?: {
    greetingEndpoint: string;
  };
}

export const RestCapabilityApiContext = React.createContext(mockRestCapabilityApi);

export function RestCapability(props: RestCapabilityProps) {
  const api = React.useContext(RestCapabilityApiContext);
  const [requests, addRequestEntry] = useRequestsState();
  const [name, setName] = React.useState<string>('');

  const execGet = () => api.doGetGreeting(name);
  return (
    <CapabilityCard module="rest">
      <CapabilityCard.Title>{capabilitiesConfig.rest.icon} {capabilitiesConfig.rest.name}</CapabilityCard.Title>
      <CapabilityCard.Body>
        <Grid>
          <GridItem span={12}>
            HTTP API endpoints expose your application to outside callers.
            Through these, programs may communicate over the network in a language-independent fashion.
            We have created an initial set of endpoints to illustrate how you may accomplish this in your selected runtime.
            By composing together HTTP endpoints and making use of hypermedia and links,
            you may follow these patterns to construct a RESTful architecture.
            </GridItem>
          <CapabilityCard.Separator />
          <GridItem span={12} className="http-request-service">
            <RequestTitle>
              <SourceMappingLink
                sourceRepository={props.sourceRepository}
                name="greetingEndpoint"
                fileRepositoryLocation={props.sourceMapping && props.sourceMapping.greetingEndpoint}
              />
            </RequestTitle>
          </GridItem>
          <HttpRequest
            name="GET Greetings"
            method="GET"
            url={api.getGreetingAbsoluteUrl(name)}
            path={`${REST_GREETING_PATH}?name=`}
            execute={execGet}
            onRequestResult={addRequestEntry}
          >
            <TextInput
              id="http-api-param-name-input"
              aria-label="Greetings name input"
              value={name}
              onChange={e => setName(e)}
              name="name"
              placeholder="World"
              className="http-request-param"
            />
          </HttpRequest>
          <GridItem span={12}>
            <RequestsConsole name="REST" requests={requests} />
          </GridItem>
        </Grid>
      </CapabilityCard.Body>
    </CapabilityCard>
  );
}
