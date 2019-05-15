import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { FormPanel } from '../../core/form-panel/form-panel';

import { ExamplePicker } from '../example-picker';
import { ExamplesLoader } from '../../loaders/example-catalog-loader';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';

storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('ExamplePicker', () => {
    return (
      <ExamplesLoader>
        {missions => (
          <FormPanel
            initialValue={{}}
            validator={ExamplePicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {(inputProps) => (<ExamplePicker.Element {...inputProps} missions={missions}/>)}
          </FormPanel>
        )}
      </ExamplesLoader>
    );
  });
