import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { FormPanel } from '../../core/form-panel/form-panel';
import { RuntimePicker } from '../runtime-picker';
import { NewAppRuntimesLoader } from '../../loaders/new-app-runtimes-loaders';
import { LauncherDepsProvider } from '../..';

storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('RuntimePicker: frontend', () => {
    return (
      <NewAppRuntimesLoader category="frontend">
        {items => (
          <FormPanel
            initialValue={{}}
            validator={RuntimePicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {
              (inputProps) => (<RuntimePicker.Element {...inputProps} items={items}/>)}
          </FormPanel>
        )}
      </NewAppRuntimesLoader>
    );
  })
  .add('RuntimePicker: backend', () => {
    return (
      <NewAppRuntimesLoader category="backend">
        {items => (
          <FormPanel
            initialValue={{}}
            validator={RuntimePicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {
              (inputProps) => (<RuntimePicker.Element {...inputProps} items={items}/>)}
          </FormPanel>
        )}
      </NewAppRuntimesLoader>
    );
  });
