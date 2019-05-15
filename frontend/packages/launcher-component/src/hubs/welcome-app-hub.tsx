import * as React from 'react';
import { OverviewEmpty } from '../core/hub-n-spoke/overview-empty';
import { FormHub } from '../core/types';
import { FormPanel } from '../core/form-panel/form-panel';
import { DescriptiveHeader } from '../core/stuff';
import { DataListItem, DataListCheck, DataListCell, Title, Button, DataList } from '@patternfly/react-core';

export interface WelcomeFormValue {
  selected?: boolean;
}

export const WelcomeAppHub: FormHub<WelcomeFormValue> = {
  id: 'welcome-app',
  title: 'Welcome Application',
  checkCompletion: value => value.selected !== undefined,
  Overview: props => (
    <OverviewEmpty
      id={WelcomeAppHub.id}
      title={`Welcome Application is ${props.value.selected ? 'enabled' : 'disabled'}`}
      action={<Button variant="primary" onClick={props.onClick}>Configure Welcome App</Button>}
    >
      We will prepare a set of examples to let you directly start playing with your new application.
      Those examples are there to get you started,
      you will be able to easily remove them once created and start developing your awesome application.
    </OverviewEmpty>
  ),
  Form: props => {
    return (
      <FormPanel
        id={WelcomeAppHub.id}
        initialValue={props.initialValue}
        validator={WelcomeAppHub.checkCompletion}
        onSave={props.onSave}
        onCancel={props.onCancel}
      >
        {
          (inputProps) => {
            const onChange = () => {
              inputProps.onChange({ selected: !inputProps.value.selected })
            };

            return (
              <React.Fragment>
                <DescriptiveHeader
                  description="Choose if you want us to perpare a set of examples to let you directly start playing with your new application"
                />
                <DataList aria-label="select-welcome-app">
                  <DataListItem aria-labelledby="welcome app" style={inputProps.value.selected ? { borderLeft: '2px solid #007bba' } : {}}>
                    <DataListCheck
                      aria-labelledby="welcome app check"
                      aria-label="welcome app check"
                      name="Selection item check"
                      onChange={onChange}
                      checked={inputProps.value.selected}
                    />
                    <DataListCell width={3} onClick={onChange} style={{ cursor: 'pointer' }}>
                      <Title size="lg" id="welcome app check" aria-label="Pick welcome app capability">Welcome app</Title>
                    </DataListCell>
                  </DataListItem>
                </DataList>
              </React.Fragment>
            )
          }}
      </FormPanel>
    );
  }
};
