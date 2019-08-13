import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { LaunchNextSteps } from '../launch-next-steps';
import { action } from '@storybook/addon-actions';
import { DownloadNextSteps } from '../download-next-steps';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';

storiesOf('Misc', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('LaunchNextSteps', () => {
    return (
      <LaunchNextSteps onClose={action('close')}/>
    );
  })
  .add('DownloadNextSteps', () => {
    return (
      <DownloadNextSteps onClose={action('close')}/>
    );
  });
