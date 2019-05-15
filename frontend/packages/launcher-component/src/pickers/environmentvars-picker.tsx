import React, { Fragment } from 'react';
import { TextInput, Button, Split, SplitItem } from '@patternfly/react-core';
import { PlusCircleIcon, TrashIcon } from '@patternfly/react-icons';

import { InputProps, Picker } from '../core/types';

const VALID_ENV_KEY_REGEXP = new RegExp('^$|^[-._a-zA-Z][-._a-zA-Z0-9]*$');

export interface EnvironmentVarsPickerValue {
  envVars?: string[][];
}

interface EnvironmentVarsPickerProps extends InputProps<EnvironmentVarsPickerValue> {
}

const NEW_ENTRY = ['', ''];

export const EnvironmentVarsPicker: Picker<EnvironmentVarsPickerProps, EnvironmentVarsPickerValue> = {
  checkCompletion: value => !!value.envVars
    && value.envVars.filter(entry => !VALID_ENV_KEY_REGEXP.test(entry[0])).length === 0,
  Element: props => {
    const entries = props.value.envVars && props.value.envVars.length > 0 ? props.value.envVars: [NEW_ENTRY];
    const isValid: (value: string) => boolean = value => VALID_ENV_KEY_REGEXP.test(value || '');
    return (
      <Fragment>
        {entries.map((entry, index) => {
            return (
              <Split key={'split' + index} gutter="sm">
                <SplitItem isMain key={'split-name' + index}>
                  <TextInput
                    isRequired
                    type="text"
                    key={'name' + index}
                    id={'env-var-name' + index}
                    name="env-var-name"
                    placeholder="Type the environment variable name"
                    pattern="[-._a-zA-Z][-._a-zA-Z0-9]*"
                    onChange={newKey => {
                      const newEntries = entries.slice();
                      newEntries[index] = [newKey, entry[1]];
                      props.onChange({envVars: newEntries});
                    }}
                    isValid={isValid(entry[0])}
                    value={entry[0]}
                  />
                </SplitItem>
                <SplitItem isMain key={'split-value' + index}>
                  <TextInput
                    isRequired
                    type="text"
                    key={'value' + index}
                    id={'env-var-value' + index}
                    name="env-var-value"
                    placeholder="Type the environment variable value"
                    onChange={newValue => {
                      const newEntries = entries.slice();
                      newEntries[index] = [entry[0], newValue];
                      props.onChange({envVars: newEntries});
                    }}
                    value={entry[1]}
                  />
                </SplitItem>
                <SplitItem isMain={false} key={'split-button' + index}>
                  <Button
                    onClick={() => {
                      let newEntries: string[][];
                      if (entries.length <= 1) {
                        newEntries = [NEW_ENTRY];
                      } else {
                        newEntries = entries.slice();
                        newEntries.splice(index, 1);
                      }
                      props.onChange({envVars: newEntries});
                    }}
                  >
                    <TrashIcon/>
                  </Button>
                </SplitItem>
              </Split>);
          }
        )}
        <Button onClick={() => props.onChange({envVars: [...entries, NEW_ENTRY]})}><PlusCircleIcon/></Button>
      </Fragment>
    );
  }
};
