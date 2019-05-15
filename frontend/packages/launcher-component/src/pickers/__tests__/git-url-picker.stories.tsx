import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { FormPanel } from '../../core/form-panel/form-panel';
import { GitUrlPicker } from '../git-url-picker';

storiesOf('Pickers', module)
  .add('GitUrlPicker', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={GitUrlPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {(inputProps) => (<GitUrlPicker.Element {...inputProps} />)}
      </FormPanel>
    );
  });
