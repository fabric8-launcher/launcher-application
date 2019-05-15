import { EmptyState, EmptyStateBody, Title } from '@patternfly/react-core';
import * as React from 'react';

export function OverviewEmpty(props: { id: string; title: string; action: React.ReactNode; children?: React.ReactNode }) {
  return (
    <EmptyState aria-label={`${props.id} is not configured`}>
      <Title size="lg">{props.title}</Title>
      <EmptyStateBody>
        {props.children}
      </EmptyStateBody>
      {props.action}
    </EmptyState>
  );
}
