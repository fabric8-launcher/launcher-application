import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { StateLauncher } from '../launcher';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';

storiesOf('Launcher', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('component', () => {
    return (
      <StateLauncher/>
    );
  });
