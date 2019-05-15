import { Grid, GridItem } from '@patternfly/react-core';
import * as React from 'react';
import { ExternalLink } from '../../../shared/components/ExternalLink';
import { useRequestsState, HttpRequest, RequestsConsole } from '../../../shared/components/HttpRequest';
import { RequestTitle } from '../../../shared/components/RequestTitle';
import CapabilityCard from '../../components/CapabilityCard';
import capabilitiesConfig from '../../config/capabilitiesConfig';
import { HEALTHCHECKS_LIVENESS_PATH, HEALTHCHECKS_READINESS_PATH, mockHealthChecksCapabilityApi } from './HealthChecksCapabilityApi';

export interface HealthChecksCapabilityProps {
}

export const HealthChecksApiContext = React.createContext(mockHealthChecksCapabilityApi);

export function HealthChecksCapability(props: HealthChecksCapabilityProps) {
  const api = React.useContext(HealthChecksApiContext);
  const [requests, addRequestEntry] = useRequestsState();
  const execGetLiveness = () => api.doGetLiveness();
  const execGetReadiness = () =>  api.doGetReadiness();
  return (
    <CapabilityCard module="healthchecks">
      <CapabilityCard.Title>{capabilitiesConfig.healthchecks.icon} {capabilitiesConfig.healthchecks.name}</CapabilityCard.Title>
      <CapabilityCard.Body>
        <Grid>
          <GridItem span={12}>
            <p>
              Health checks are HTTP endpoints that the codebase exposes for the purpose of monitoring.
              OpenShift will periodically hit these to determine that the container is functioning properly.
              If not, it may kill and/or restart the pod to maintain the targeted number of instances configured to be running.
              </p>
            <p>Your application has been set up with the following Health Checks.</p>
            <p>
              <ExternalLink
                href="https://docs.openshift.com/container-platform/latest/dev_guide/application_health.html"
              >
                Learn more
              </ExternalLink>
            </p>
          </GridItem>
          <CapabilityCard.Separator />
          <GridItem span={12} className="http-request-service">
            <RequestTitle>Liveness</RequestTitle>
          </GridItem>
          <HttpRequest
            name="Liveness check"
            method="GET"
            url={api.getLivenessAbsoluteUrl()}
            path={HEALTHCHECKS_LIVENESS_PATH}
            execute={execGetLiveness}
            onRequestResult={addRequestEntry}
          />
          <GridItem span={12} className="http-request-service">
            <RequestTitle>Readiness</RequestTitle>
          </GridItem>
          <HttpRequest
            name="Readiness check"
            method="GET"
            path={HEALTHCHECKS_READINESS_PATH}
            url={api.getReadinessAbsoluteUrl()}
            execute={execGetReadiness}
            onRequestResult={addRequestEntry}
          />
          <GridItem span={12}>
            <RequestsConsole name="HealthChecks" requests={requests} />
          </GridItem>
        </Grid>
      </CapabilityCard.Body>
    </CapabilityCard>
  );
}
