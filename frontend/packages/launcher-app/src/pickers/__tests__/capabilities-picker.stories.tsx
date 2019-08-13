import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { CapabilitiesPicker } from '../capabilities-picker';
import { FormPanel } from '../../core/form-panel/form-panel';
import { NewAppCapabilitiesLoader, capabilityToItem, readOnlyCapabilities } from '../../loaders/new-app-capabilities-loader';
import { LauncherDepsProvider } from '../..';

storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('CapabilitiesPicker', () => {
    return (
      <NewAppCapabilitiesLoader categories={['backend', 'support']}>
        {capabilities => (
          <FormPanel
            initialValue={{ capabilities: readOnlyCapabilities }}
            validator={CapabilitiesPicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {
              (inputProps) => (<CapabilitiesPicker.Element {...inputProps} items={capabilities.map(capabilityToItem)}/>)}
          </FormPanel>
        )}
      </NewAppCapabilitiesLoader>
    );
  });
