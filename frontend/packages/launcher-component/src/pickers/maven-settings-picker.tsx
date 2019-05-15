import { Form } from '@patternfly/react-core';
import React from 'react';
import { InputProps, Picker } from '../core/types';
import { LaunchTextInput } from '../core/text-input/text-input';

export interface MavenSettingsPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
}

interface MavenSettingsPickerProps extends InputProps<MavenSettingsPickerValue> {
}

const VALUE_REGEXP = /^[a-z][a-z0-9-\.]{3,63}$/;

export const MavenSettingsPicker: Picker<MavenSettingsPickerProps, MavenSettingsPickerValue> = {
  checkCompletion: value => !!value.groupId && VALUE_REGEXP.test(value.groupId)
    && !!value.artifactId && VALUE_REGEXP.test(value.artifactId) && !!value.version,
  Element: props => {
    const isValid = (value?: string) => !!value && VALUE_REGEXP.test(value || '');
    return (
      <Form>
        <LaunchTextInput
          label="GroupId"
          isRequired
          helperTextInvalid="Please provide a valid groupId"
          type="text"
          id="groupId"
          name="groupId"
          aria-label="Maven groupId name"
          value={props.value.groupId || ''}
          onChange={value => props.onChange({ ...props.value, groupId: value })}
          pattern={VALUE_REGEXP.source}
          isValid={isValid(props.value.groupId)}
        />
        <LaunchTextInput
          label="ArtifactId"
          isRequired
          helperTextInvalid="Please provide a valid artifactId"
          type="text"
          id="artifactId"
          name="artifactId"
          aria-label="Maven artifactId name"
          value={props.value.artifactId || ''}
          onChange={value => props.onChange({ ...props.value, artifactId: value })}
          pattern={VALUE_REGEXP.source}
          isValid={isValid(props.value.artifactId)}
        />
        <LaunchTextInput
          label="Version"
          helperTextInvalid="Please provide a version number"
          isRequired
          type="text"
          id="version"
          name="version"
          aria-label="Maven version number"
          value={props.value.version || ''}
          onChange={value => props.onChange({ ...props.value, version: value })}
          isValid={!!props.value.version}
        />
      </Form>
    );
  }
};
