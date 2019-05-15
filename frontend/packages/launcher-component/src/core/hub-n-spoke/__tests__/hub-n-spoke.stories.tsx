import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { HubNSpoke } from '..';
import { mockItems } from './mock-items';

storiesOf('Core', module)
  .add('HubNSpoke', () => (
    <HubNSpoke title="HubNSpoke" items={mockItems}/>
  ));
