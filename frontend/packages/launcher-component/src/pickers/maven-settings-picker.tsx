import React from 'react';
import { optionalBool } from '../core/stuff';
import { LaunchTextInput } from '../core/text-input/text-input';
import { TogglePanel } from '../core/toggle-panel/toggle-panel';
import { InputProps, Picker } from '../core/types';

export interface MavenSettingsPickerValue {
  groupId?: string;
  artifactId?: string;
  version?: string;
  name?: string;
  description?: string;
  packageName?: string;
}

interface MavenSettingsPickerProps extends InputProps<MavenSettingsPickerValue> {
  showMoreOptions?: boolean;
}

const VALUE_REGEXP = /^[a-z][a-z0-9-\.]{3,63}$/;

export const MavenSettingsPicker: Picker<MavenSettingsPickerProps, MavenSettingsPickerValue> = {
  checkCompletion: value => !!value.groupId && VALUE_REGEXP.test(value.groupId)
    && !!value.artifactId && VALUE_REGEXP.test(value.artifactId) && !!value.version,
  Element: props => {
    const isValid = (value?: string) => !!value && VALUE_REGEXP.test(value || '');
    return (
      <div className="pf-c-form">
        <LaunchTextInput
          label="Group"
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
          label="Artifact"
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
        
        {optionalBool(props.showMoreOptions, true) && (
          <TogglePanel title="Options">
            <div className="pf-c-form">
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
              <LaunchTextInput
                label="Description"
                helperTextInvalid="Please provide a description"
                isRequired
                type="text"
                id="description"
                name="description"
                aria-label="Maven description"
                value={props.value.description || ''}
                onChange={value => props.onChange({ ...props.value, description: value })}
                isValid={true}
              />
              <LaunchTextInput
                label="Package Name"
                helperTextInvalid="Please provide a package name"
                isRequired
                type="text"
                id="packageName"
                name="packageName"
                aria-label="Maven package name"
                value={props.value.packageName || ''}
                onChange={value => props.onChange({ ...props.value, packageName: value })}
                isValid={true}
              />
            </div>
          </TogglePanel>
        )}
      </div>
    );
  }
};
