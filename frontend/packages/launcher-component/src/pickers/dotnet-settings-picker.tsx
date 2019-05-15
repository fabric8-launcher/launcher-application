import { Form } from '@patternfly/react-core';
import React from 'react';
import { InputProps, Picker } from '../core/types';
import { LaunchTextInput } from '../core/text-input/text-input';

export interface DotNetSettingsPickerValue {
  namespace?: string;
  version?: string;
}

interface DotNetSettingsPickerProps extends InputProps<DotNetSettingsPickerValue> {
}

// tslint:disable-next-line:max-line-length
const VERSION_REGEXP = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*)?(\+[0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*)?$/;
const NAME_REGEXP = /^[a-zA-Z_].*$/;

export const DotNetSettingsPicker: Picker<DotNetSettingsPickerProps, DotNetSettingsPickerValue> = {
  checkCompletion: value => !!value.namespace && NAME_REGEXP.test(value.namespace)
    && !!value.version && VERSION_REGEXP.test(value.version),
  Element: props => {
    return (
      <Form>
        <LaunchTextInput
          label="Namespace"
          isRequired
          helperTextInvalid="Please provide a valid namespace"
          type="text"
          id="name"
          name="name"
          aria-label=".Net namespace"
          value={props.value.namespace || ''}
          onChange={value => props.onChange({ ...props.value, namespace: value })}
          pattern={NAME_REGEXP.source}
          isValid={NAME_REGEXP.test(props.value.namespace || '')}
        />
        <LaunchTextInput
          label="Version"
          isRequired
          helperTextInvalid="Please provide a valid version number"
          type="text"
          id="version"
          name="version"
          aria-label=".Net version"
          value={props.value.version || ''}
          onChange={value => props.onChange({ ...props.value, version: value })}
          isValid={VERSION_REGEXP.test(props.value.version || '')}
        />
      </Form >
    );
  }
};
