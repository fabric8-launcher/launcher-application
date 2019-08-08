import * as React from 'react';
import { ExamplesLoader, ExamplesLoaderWithFilter } from '../loaders/example-catalog-loader';
import { ExamplePicker, ExamplePickerValue } from '../pickers/example-picker';
import { Button } from '@patternfly/react-core';
import { FormHub, FormPanel, DescriptiveHeader, OverviewEmpty, OverviewComplete } from '@launcher/component';
import { ExampleMission } from '../client/types';

export interface ExampleFormValue {
  examplePickerValue?: ExamplePickerValue;
}

export const ExampleHub: FormHub<ExampleFormValue> = {
  id: 'example',
  title: 'Example',
  checkCompletion: value => !!value.examplePickerValue && ExamplePicker.checkCompletion(value.examplePickerValue),
  Overview: props => {
    if (!ExampleHub.checkCompletion(props.value)) {
      return (
        <OverviewEmpty
          id={ExampleHub.id}
          title="You need to select a Example"
          action={<Button variant="primary" onClick={props.onClick}>Select an Example</Button>}
        >
          You will be able to have an entire application running in a few seconds...
        </OverviewEmpty>
      );
    }
    return (
      <ExamplesLoaderWithFilter
        query={{missionId: props.value.examplePickerValue!.missionId, runtimeId: props.value.examplePickerValue!.runtimeId}}
      >
        {(result) => {
          const runtime = (result as ExampleMission).runtime![0];
          return (<OverviewComplete id={ExampleHub.id} title={`Your example will be ${result.name} using:`}>
            {runtime.icon &&
              <img src={runtime.icon} style={{ margin: '5px auto', height: '160px' }} alt={runtime.name} />}
            {!runtime.icon &&
              <h1>{runtime.name}</h1>}
          </OverviewComplete>);
        }}
      </ExamplesLoaderWithFilter>
    );
  },
  Form: props => (
    <FormPanel
      id={ExampleHub.id}
      initialValue={props.initialValue}
      validator={ExampleHub.checkCompletion}
      onSave={props.onSave}
      onCancel={props.onCancel}
    >
      {
        (inputProps) => (
          <React.Fragment>
            <DescriptiveHeader
              description="Select the use case that you want an Example of.
                        Once the use case is selected you can select the runtime implementation"
            />
            <ExamplesLoader>
              {missions => (
                <ExamplePicker.Element
                  value={inputProps.value.examplePickerValue || {}}
                  onChange={(examplePickerValue) => inputProps.onChange({...inputProps.value, examplePickerValue})}
                  missions={missions}
                />
              )}
            </ExamplesLoader>
          </React.Fragment>
        )}
    </FormPanel>
  )
};
