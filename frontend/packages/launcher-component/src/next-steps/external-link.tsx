import React, { CSSProperties } from 'react';
import { Button } from '@patternfly/react-core';
import { ExternalLinkSquareAltIcon } from '@patternfly/react-icons';

export function ExternalLink(props: {
  'aria-label'?: string;
  children: React.ReactNode;
  href: string;
  style?: CSSProperties;
}) {
  return (
    <Button
      style={props.style}
      // @ts-ignore
      component="a"
      variant="link"
      href={props.href}
      aria-label={props['aria-label']}
      target={'_blank'}
    >
        {props.children} <ExternalLinkSquareAltIcon />
    </Button>);
}
