import * as React from 'react';
import { RuntimePicker, RuntimePickerValue } from '../pickers/runtime-picker';
import { NewAppRuntimesLoader, NewAppRuntimeLoader } from '../loaders/new-app-runtimes-loaders';
import { Button } from '@patternfly/react-core';
import { FormPanel, DescriptiveHeader, OverviewEmpty, FormHub, OverviewComplete } from '@launcher/component';

export interface FrontendFormValue {
  runtimePickerValue?: RuntimePickerValue;
}

export const FrontendHub: FormHub<FrontendFormValue> = {
  id: 'frontend',
  title: 'Frontend',
  checkCompletion: value => !!value.runtimePickerValue && RuntimePicker.checkCompletion(value.runtimePickerValue),
  Overview: props => {
    if (!FrontendHub.checkCompletion(props.value)) {
      return (
        <OverviewEmpty
          id={FrontendHub.id}
          title="You can configure a Frontend for your application"
          action={<Button variant="primary" onClick={props.onClick}>Configure a Frontend</Button>}
        >
          You will be able to bootstrap the frontend of your application in a few seconds...
        </OverviewEmpty>
      );
    }
    return (
      <NewAppRuntimeLoader id={props.value.runtimePickerValue!.runtimeId!}>
        {runtime => (
          <OverviewComplete id={FrontendHub.id} title={`Your ${runtime!.name} frontend is configured`}>
            <img src={runtime!.icon} style={{margin: '5px auto', height: '160px'}} alt={runtime!.name}/>
          </OverviewComplete>
        )}
      </NewAppRuntimeLoader>
    );
  },
  Form: props => (
    <FormPanel
      id={FrontendHub.id}
      initialValue={props.initialValue}
      // We don't check completion because no backend (with a frontend) is valid
      onSave={props.onSave}
      onCancel={props.onCancel}
    >
      {
        (inputProps) => (
          <React.Fragment>
            <DescriptiveHeader
              description="You may optionally select a frontend application to bootstrap your web-based development.
                        These options scaffold a starting point in your framework of choice."
            />
            <NewAppRuntimesLoader category="frontend">
              {(items) => (
                <RuntimePicker.Element
                  items={items}
                  value={inputProps.value.runtimePickerValue || {}}
                  onChange={(runtimePickerValue) => inputProps.onChange({...inputProps.value, runtimePickerValue})}
                />
              )}
            </NewAppRuntimesLoader>
          </React.Fragment>
        )}
    </FormPanel>
  ),
};
