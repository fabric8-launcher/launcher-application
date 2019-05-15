import React, { createRef, useEffect } from 'react';
import { Split, SplitItem, Button, TextInput, FormGroup } from '@patternfly/react-core';
import { EditIcon } from '@patternfly/react-icons';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';

import { InputProps, Picker } from '../core/types';

export interface GitUrlPickerValue {
  temp?: string;
  url?: string;
}

interface GitUrlPickerProps extends InputProps<GitUrlPickerValue> {
}

const VALUE_REGEXP = /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&\/=]*)$/

export const GitUrlPicker: Picker<GitUrlPickerProps, GitUrlPickerValue> = {
  checkCompletion: value => !!value.url && VALUE_REGEXP.test(value.url),
  Element: props => {
    const [check, setCheck] = useSessionStorageWithObject<boolean>('check', false);
    const textRef = createRef<any>();
    const isValid = (value?: string) => !!value && VALUE_REGEXP.test(value || '');
    const changeValue = () => {
      const value = { ...props.value, url: textRef.current!.props.value.length > 0 ? textRef.current!.props.value.trim() : undefined };
      props.onChange(value);
      setCheck(true);
    };
    const clear = () => {
      props.onChange({ ...props.value, url: '' });
      setCheck(false);
    };
    useEffect(() => {
      return () => {
        setCheck(false);
      }
    }, []);
    return (
      <Split>
        <SplitItem isMain>
          <FormGroup
            fieldId="git-url-picker"
            isValid={isValid(props.value.temp)}
            helperTextInvalid="Please provide a valid url that uses http"
          >
            <TextInput
              isRequired
              type="text"
              id="git-url-picker"
              name="git-url-picker"
              aria-label="Git repository url"
              placeholder="Type the git repository url"
              isDisabled={check}
              ref={textRef}
              onChange={value => props.onChange({ ...props.value, temp: value })}
              value={props.value.temp || ''}
              isValid={isValid(props.value.temp)}
            />
          </FormGroup>
        </SplitItem>
        <SplitItem isMain={false}>
          {!check && <Button
            onClick={changeValue}
            isDisabled={!isValid(props.value.temp)}
          >Done
          </Button>}
          {check && <Button
            onClick={clear}
          ><EditIcon />
          </Button>}
        </SplitItem>
      </Split>
    );
  }
};
