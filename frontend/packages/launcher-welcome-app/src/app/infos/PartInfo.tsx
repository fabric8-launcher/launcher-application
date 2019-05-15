import { Grid, GridItem } from '@patternfly/react-core';
import { ServicesIcon, ScreenIcon } from '@patternfly/react-icons';
import * as React from 'react';
import InfoCard from '../components/InfoCard';
import { ExternalLink } from '../../shared/components/ExternalLink';
import { getRouteLink } from '../config/appConfig';
import DockerImageLink from '../components/DockerImageLink';
import { capitalizeFirstLetter } from '../../shared/utils/Strings';

interface PartInfoProps {
  subfolderName?: string;
  category: 'backend' | 'frontend' | 'support';
  runtimeInfo: {
    enumInfo: {
      id: string;
      name: string;
      description: string;
      icon: string;
      metadata: { language: string; };
    }
    image: string;
    route: string;
    service: string;
  };
}

const categoryDefinitionIcon = {
  backend: <ServicesIcon className="with-text"/>,
  frontend: <ScreenIcon className="with-text"/>,
} as {[part: string]: React.ReactNode};

export function PartInfo(props: PartInfoProps) {
  const link = getRouteLink(props.runtimeInfo.route);
  const language = capitalizeFirstLetter(props.runtimeInfo.enumInfo.metadata.language);
  const category = capitalizeFirstLetter(props.category);
  return (
    <InfoCard name={props.category + '-tier'}>
      <InfoCard.Title>{categoryDefinitionIcon[props.category]}
        {category} - {props.runtimeInfo.enumInfo.name} - {language}
      </InfoCard.Title>
      <InfoCard.Body>
        <Grid>
          <GridItem span={3}>
            <img src={props.runtimeInfo.enumInfo.icon}/>
          </GridItem>
          <GridItem span={9}>
            <h1>{props.runtimeInfo.enumInfo.name} - {language}</h1>
            <p className="description">{props.runtimeInfo.enumInfo.description}</p>
          </GridItem>
          <InfoCard.Separator/>
          <GridItem span={3}>Runtime Image</GridItem><GridItem span={9}><DockerImageLink image={props.runtimeInfo.image}/></GridItem>
          <InfoCard.Separator/>
          <GridItem span={3}>Service name</GridItem><GridItem span={9}>
          <span className="monospace">{props.runtimeInfo.service}</span>
        </GridItem>
          <InfoCard.Separator/>
          <GridItem span={3}>Route</GridItem><GridItem span={9}><ExternalLink href={link}>{link}</ExternalLink></GridItem>
        </Grid>
      </InfoCard.Body>
    </InfoCard>
  );
}
