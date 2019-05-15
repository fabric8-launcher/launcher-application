import * as React from 'react';
import { ExternalLink } from './ExternalLink';

export function SourceMappingLink(props: {
  sourceRepository?: {
    url: string;
    provider: string;
  };
  name: string;
  fileRepositoryLocation?: string;
}) {
  if (!!props.sourceRepository && props.sourceRepository && props.sourceRepository.provider.toLowerCase() === 'github' && props.fileRepositoryLocation) {
    const link = `${props.sourceRepository.url.replace('.git', '')}/blob/master/${props.fileRepositoryLocation}`;
    const fileName = props.fileRepositoryLocation.replace(/^.*\//, '');
    return (<span>{fileName} (<ExternalLink href={link}>view source</ExternalLink>)</span>);
  }
  return (<span title={props.fileRepositoryLocation || props.name}>{props.name}</span>);
}
