import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { FormPanel } from '../../core/form-panel/form-panel';

import { DotNetSettingsPicker } from '../dotnet-settings-picker';

storiesOf('Pickers', module)
  .add('DotNetSettingsPicker ', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={DotNetSettingsPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {
          (inputProps) => (<DotNetSettingsPicker.Element {...inputProps} />)}
      </FormPanel>
    );
  });
