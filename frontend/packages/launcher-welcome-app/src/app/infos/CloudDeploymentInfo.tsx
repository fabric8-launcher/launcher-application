import { Grid, GridItem } from '@patternfly/react-core';
import * as React from 'react';
import { ExternalLink } from '../../shared/components/ExternalLink';

import openshiftImage from '../../assets/img/openshift.png';
import InfoCard from '../components/InfoCard';
import { CloudIcon } from '@patternfly/react-icons';

interface CloudDeploymentInfoProps {
  applicationName: string;
  applicationUrl: string;
  openshiftConsoleUrl?: string;
}

export function CloudDeploymentInfo(props: CloudDeploymentInfoProps) {
  const projectUrl = props.openshiftConsoleUrl && `${props.openshiftConsoleUrl}/console/project/${props.applicationName}`;
  return (
    <InfoCard name="cloud-deployment">
      <InfoCard.Title><CloudIcon className="with-text"/> Cloud Deployment</InfoCard.Title>
      <InfoCard.Body>
        <Grid>
          <GridItem span={3}>
            <img src={openshiftImage}/>
          </GridItem>
          <GridItem span={9}>
            <p>This system is deployed on the OpenShift Cloud Platform,
              a Kubernetes distribution which builds and runs the applications and services we've created.</p>
          </GridItem>
          <InfoCard.Separator/>
          <GridItem span={3}>Application Name</GridItem>
          <GridItem span={9}>
            <span className="monospace">{props.applicationName}</span>
          </GridItem>
          <InfoCard.Separator/>
          <GridItem span={3}>Application URL</GridItem>
          <GridItem span={9}>
            <ExternalLink href={props.applicationUrl}>{props.applicationUrl}</ExternalLink>
            <br/>
            The link to this application running live on OpenShift
          </GridItem>
          {projectUrl && (
            <React.Fragment>
              <InfoCard.Separator/>
              <GridItem span={3}>OpenShift Console URL</GridItem>
              <GridItem span={9}>
                <ExternalLink href={projectUrl}>{projectUrl}</ExternalLink>
                <br/>
                Link to manage the OpenShift instance where this application is deployed.
              </GridItem>
            </React.Fragment>
          )}
        </Grid>
      </InfoCard.Body>
    </InfoCard>
  );
}
