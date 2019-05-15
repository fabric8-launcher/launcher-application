import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { FormPanel } from '../../core/form-panel/form-panel';

import { MavenSettingsPicker } from '../maven-settings-picker';

storiesOf('Pickers', module)
  .add('MavenSettingsPicker ', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={MavenSettingsPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {
          (inputProps) => (<MavenSettingsPicker.Element {...inputProps} />)}
      </FormPanel>
    );
  });
