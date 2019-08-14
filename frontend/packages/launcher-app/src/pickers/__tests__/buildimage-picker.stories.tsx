import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { BuildImageAnalyzerLoader } from '../../loaders/buildimage-loader';
import { BuildImagePicker } from '../buildimage-picker';
import { FormPanel } from '@launcher/component';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';

storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('BuildImagePicker', () => {
    return (
      <BuildImageAnalyzerLoader gitUrl="https://github.com/fabric8-launcher/launcher-frontend">
        {result => (
          <FormPanel
            initialValue={{}}
            validator={BuildImagePicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {
              (inputProps) => (
                <BuildImagePicker.Element {...inputProps} builderImages={result.builderImages} suggestedImageName={result.image} />
              )}
          </FormPanel>
        )}
      </BuildImageAnalyzerLoader>
    );
  });
