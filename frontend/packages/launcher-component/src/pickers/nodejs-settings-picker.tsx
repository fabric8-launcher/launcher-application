import { Form } from '@patternfly/react-core';
import React from 'react';
import { InputProps, Picker } from '../core/types';
import { LaunchTextInput } from '../core/text-input/text-input';

export interface NodeJSSettingsPickerValue {
  name?: string;
  version?: string;
}

interface NodeJSSettingsPickerProps extends InputProps<NodeJSSettingsPickerValue> {
}

// tslint:disable-next-line:max-line-length
const VERSION_REGEXP = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*)?(\+[0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*)?$/;
const NAME_REGEXP = /^(?=.{1,214}$)(?:@[a-z0-9-~][a-z0-9-._~]*\/)?[a-z0-9-~][a-z0-9-._~]*$/;

export const NodeJSSettingsPicker: Picker<NodeJSSettingsPickerProps, NodeJSSettingsPickerValue> = {
  checkCompletion: value => !!value.name && NAME_REGEXP.test(value.name)
    && !!value.version && VERSION_REGEXP.test(value.version),
  Element: props => {
    return (
      <Form>
        <LaunchTextInput
          label="Name"
          isRequired
          helperTextInvalid="Please provide a valid name"
          type="text"
          id="name"
          name="name"
          aria-label="Nodejs package name"
          value={props.value.name || ''}
          onChange={value => props.onChange({ ...props.value, name: value })}
          pattern={NAME_REGEXP.source}
          isValid={NAME_REGEXP.test(props.value.name || '')}
        />
        <LaunchTextInput
          label="Version"
          isRequired
          helperTextInvalid="Please provide a valid version number"
          type="text"
          id="version"
          name="version"
          aria-label="Nodejs version"
          value={props.value.version || ''}
          onChange={value => props.onChange({ ...props.value, version: value })}
          isValid={VERSION_REGEXP.test(props.value.version || '')}
        />
      </Form >
    );
  }
};
