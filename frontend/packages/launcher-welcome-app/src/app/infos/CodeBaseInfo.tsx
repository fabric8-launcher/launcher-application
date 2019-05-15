import { Grid, GridItem } from '@patternfly/react-core';
import * as React from 'react';
import { ExternalLink } from '../../shared/components/ExternalLink';
import InfoCard from '../components/InfoCard';
import { CodeIcon } from '@patternfly/react-icons';
import ShellCommand from '../../shared/components/ShellCommand';

interface CodeBaseInfoProps {
  sourceRepository: {
    url: string;
    provider: string;
  };
}

export function CodeBaseInfo(props: CodeBaseInfoProps) {
  return (
    <InfoCard name="codebase">
      <InfoCard.Title><CodeIcon className="with-text"/> Codebase</InfoCard.Title>
      <InfoCard.Body>
        <Grid>
          <GridItem span={3}>Git URL</GridItem>
          <GridItem span={9}>
            <ExternalLink href={props.sourceRepository.url}>{props.sourceRepository.url}</ExternalLink>
            <br/>
            You may use the git command-line client (or other preferred tooling) to clone this repository locally.
            <ShellCommand command={`git clone ${props.sourceRepository.url}`}/>
          </GridItem>
        </Grid>
      </InfoCard.Body>
    </InfoCard>
  );
}
