import React from 'react';
import { Form, FormGroup, FormSelect, FormSelectOption, Grid, GridItem } from '@patternfly/react-core';
import { GitInfo } from '@launcher/client';

import { InputProps, Picker } from '../core/types';
import { BanIcon } from '@patternfly/react-icons';
import { LaunchTextInput } from '../core/text-input/text-input';
import style from './repository-picker.module.scss';

const REPOSITORY_VALUE_REGEXP = /^[a-z][a-z0-9-.]{3,63}$/;

export interface UserRepositoryPickerValue {
  org?: string;
  name?: string;
  isDuplicate?: boolean;
}

interface UserRepositoryPickerProps extends InputProps<UserRepositoryPickerValue> {
  gitInfo: GitInfo;
}

export function valueToPath(value: UserRepositoryPickerValue, login?: string) {
  if (value.org) {
    return `${value.org}/${value.name}`;
  }
  if (login) {
    return `${login}/${value.name}`;
  }
  return value.name || '';
}

const isDuplicate = (props: UserRepositoryPickerProps): boolean => {
  return !!props.gitInfo.login && props.gitInfo.repositories.indexOf(valueToPath(props.value, props.gitInfo.login)) !== -1;
};

export const UserRepositoryPicker: Picker<UserRepositoryPickerProps, UserRepositoryPickerValue> = {
  checkCompletion: value => !value.isDuplicate
    && (!value.org || REPOSITORY_VALUE_REGEXP.test(value.org)) && REPOSITORY_VALUE_REGEXP.test(value.name || ''),
  Element: props => {
    const name = props.value.name || '';
    const helperRepoInvalid = isDuplicate(props) ?
      `Repository already exists ${valueToPath(props.value, props.gitInfo.login)}` : 'Invalid repository name';
    const onChange = (value) => {
      props.onChange({ ...value, isDuplicate: isDuplicate({ ...props, value: { ...value } }) });
    };
    return (
      <Grid>
        <GridItem span={4} className={style.avatar}>
          {props.gitInfo.login && <span>
            <img src={props.gitInfo.avatarUrl} />
            <p><b>{props.gitInfo.login}</b></p>
          </span>
          }
          {!props.gitInfo.login && <span>
            <BanIcon size="xl" />
            <p><b>None</b></p>
          </span>
          }

        </GridItem>
        <GridItem span={8}>
          <h3>Choose a Repository</h3>
          <Form>
            <FormGroup
              label="Location"
              isRequired
              fieldId="ghOrg"
            >
              <FormSelect
                id="ghOrg"
                value={props.value.org}
                onChange={value => value ? onChange({ ...props.value, org: value })
                  : onChange({ name: props.value.name })}
                isDisabled={!props.gitInfo.login}
                aria-label="Select organization"
              >
                <FormSelectOption
                  value={undefined}
                  label={props.gitInfo.login || 'Not connected to source repository'}
                />
                {props.gitInfo.login && props.gitInfo.organizations.map((o, index) => (
                  <FormSelectOption
                    key={index}
                    value={o}
                    label={o}
                  />)
                )}
              </FormSelect>
            </FormGroup>
            <LaunchTextInput
              label="Repository"
              isRequired
              helperTextInvalid={helperRepoInvalid}
              isValid={UserRepositoryPicker.checkCompletion(props.value)}
              type="text"
              id="ghRepo"
              name="ghRepo-name"
              placeholder="Select Repository"
              aria-label="Select Repository"
              onChange={value => onChange({ ...props.value, name: value })}
              value={name}
              pattern={REPOSITORY_VALUE_REGEXP.source}
              title="Valid repository name"
              isDisabled={!props.gitInfo.login}
            />
          </Form>
        </GridItem>
      </Grid>
    );
  }
};
