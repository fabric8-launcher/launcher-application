import '@patternfly/react-core/dist/styles/base.css';
import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import React from 'react';
import { LauncherDepsProvider } from '../..';
import { FormPanel } from '../../core/form-panel/form-panel';
import { EnumLoader } from '../../loaders/enum-loader';
import { DependenciesPicker, DependencyItem } from '../dependencies-picker';


storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('DependenciesPicker', () => {
    return (
      <EnumLoader name="quarkus-extensions">
        {dependencies => (
          <FormPanel
            initialValue={{}}
            validator={DependenciesPicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {
              (inputProps) => (<DependenciesPicker.Element {...inputProps} items={dependencies as DependencyItem[]} placeholder="cat1, cat2, ..."/>)}
          </FormPanel>
        )}
      </EnumLoader>
    );
  });