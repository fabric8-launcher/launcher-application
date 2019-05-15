import * as React from 'react';
import { ExternalLinkSquareAltIcon } from '@patternfly/react-icons';

export function ExternalLink(props: {
  children: React.ReactNode;
  href: string
}) {
  return (<a href={props.href} target={'_blank'}>{props.children} <ExternalLinkSquareAltIcon/></a>);
}
