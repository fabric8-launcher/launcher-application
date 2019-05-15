import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { FormPanel } from '../../core/form-panel/form-panel';

import { NodeJSSettingsPicker } from '../nodejs-settings-picker';

storiesOf('Pickers', module)
  .add('NodeJSSettingsPicker ', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={NodeJSSettingsPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {
          (inputProps) => (<NodeJSSettingsPicker.Element {...inputProps} />)}
      </FormPanel>
    );
  });
