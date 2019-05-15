import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { BackendHub } from '../backend-hub';
import { FrontendHub } from '../frontend-hub';
import { DestRepositoryHub } from '../dest-repository-hub';
import { ExampleHub } from '../example-hub';
import { SrcRepositoryHub } from '../src-repository-hub';
import { LauncherDepsProvider } from '../..';
import { readOnlyCapabilities } from '../../loaders/new-app-capabilities-loader';
import { WelcomeAppHub } from '../welcome-app-hub';

storiesOf('Forms', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('BackendForm', () => {
    return (
      <BackendHub.Form
        initialValue={{capabilitiesPickerValue: {capabilities: readOnlyCapabilities}}}
        onSave={action('save')}
        onCancel={action('cancel')}
      />
    );
  })
  .add('FrontendForm', () => {
    return (
      <FrontendHub.Form initialValue={{}} onSave={action('save')} onCancel={action('cancel')}/>
    );
  })
  .add('DestRepositoryForm', () => {
    return (
      <DestRepositoryHub.Form initialValue={{}} onSave={action('save')} onCancel={action('cancel')}/>
    );
  })
  .add('ExampleForm', () => {
    return (
      <ExampleHub.Form initialValue={{}} onSave={action('save')} onCancel={action('cancel')}/>
    );
  })
  .add('SrcRepositoryForm', () => {
    return (
      <SrcRepositoryHub.Form initialValue={{}} onSave={action('save')} onCancel={action('cancel')}/>
    );
  })
  .add('WelcomeAppForm', () => {
    return (
      <WelcomeAppHub.Form initialValue={{}} onSave={action('save')} onCancel={action('cancel')}/>
    );
  });
