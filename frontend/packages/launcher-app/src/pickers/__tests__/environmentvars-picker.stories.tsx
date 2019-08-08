import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { EnvironmentVarsPicker } from '../environmentvars-picker';
import { FormPanel } from '@launcher/component';

storiesOf('Pickers', module)
  .add('EnvironmentVarsPicker', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={EnvironmentVarsPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {
          (inputProps) => (<EnvironmentVarsPicker.Element {...inputProps} />)}
      </FormPanel>
    );
  });
