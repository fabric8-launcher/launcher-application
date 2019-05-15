import { CapabilitiesPicker, CapabilitiesPickerValue } from '../pickers/capabilities-picker';
import { DescriptiveHeader, Separator, SpecialValue } from '../core/stuff';
import * as React from 'react';
import { RuntimePicker, RuntimePickerValue } from '../pickers/runtime-picker';
import { NewAppRuntimesLoader, NewAppRuntimeLoader } from '../loaders/new-app-runtimes-loaders';
import { NewAppCapabilitiesByModuleLoader, NewAppCapabilitiesLoader, capabilityToItem } from '../loaders/new-app-capabilities-loader';
import { FormPanel } from '../core/form-panel/form-panel';
import { FormHub } from '../core/types';
import { Button, List, ListItem, Split, SplitItem, Text, TextVariants } from '@patternfly/react-core';
import { OverviewComplete } from '../core/hub-n-spoke/overview-complete';
import { OverviewEmpty } from '../core/hub-n-spoke/overview-empty';

export interface BackendFormValue {
  runtimePickerValue?: RuntimePickerValue;
  capabilitiesPickerValue?: CapabilitiesPickerValue;
}

export const BackendHub: FormHub<BackendFormValue> = {
  id: 'backend',
  title: 'Backend',
  checkCompletion: value => {
    return !!value.runtimePickerValue && RuntimePicker.checkCompletion(value.runtimePickerValue)
      && !!value.capabilitiesPickerValue && CapabilitiesPicker.checkCompletion(value.capabilitiesPickerValue);
  },
  Overview: props => {
    if (!BackendHub.checkCompletion(props.value)) {
      return (
        <OverviewEmpty
          id={BackendHub.id}
          title="You can configure a Backend for your application"
          action={<Button variant="primary" onClick={props.onClick}>Configure a Backend</Button>}
        >
            By selecting a bunch of capabilities (Http Api, Database, ...), you will be able to bootstrap the backend of
            your application in a few seconds...
        </OverviewEmpty>
      );
    }
    return (
      <NewAppRuntimeLoader id={props.value.runtimePickerValue!.runtimeId!}>
        {runtime => (
          <OverviewComplete id={BackendHub.id} title={`Your ${runtime!.name} backend is configured`}>
            <Split>
              <SplitItem isMain={false}>
                <img src={runtime!.icon} style={{marginRight: '20px', height: '75px'}}/>
              </SplitItem>
              <SplitItem isMain={true}>
                <NewAppCapabilitiesByModuleLoader categories={['backend', 'support']}>
                  {capabilitiesById => (
                    <div style={{textAlign: 'left'}}>
                      <Text component={TextVariants.p} style={{marginBottom: '10px'}}>Featuring</Text>
                      <List variant="grid" style={{listStyleType: 'none'}}>
                        {props.value.capabilitiesPickerValue!.capabilities!.filter(c => c.selected)
                          .map(c => (
                              <ListItem key={c.id}>
                                <img
                                  src={capabilitiesById.get(c.id)!.metadata.icon}
                                  style={{marginRight: '10px', verticalAlign: 'middle'}}
                                />
                                <SpecialValue>{capabilitiesById.get(c.id)!.name}</SpecialValue>
                              </ListItem>
                            )
                          )
                        }
                      </List>
                    </div>
                  )}
                </NewAppCapabilitiesByModuleLoader>
              </SplitItem>
            </Split>
          </OverviewComplete>
        )}
      </NewAppRuntimeLoader>
    );
  },
  Form: props => {
    return (
      <FormPanel
        id={BackendHub.id}
        initialValue={props.initialValue}
        // We don't check completion because no backend (with a frontend) is valid
        onSave={props.onSave}
        onCancel={props.onCancel}
      >
        {
          (inputProps) => (
            <React.Fragment>
              <DescriptiveHeader
                title="Runtime"
                description="Runtimes power the server-side processing of your application,
                       and we can get you set up in one of several languages and frameworks.
                       If you're looking to expose an HTTP API or interact with services like a database,
                       choosing one here will hook that together for you."
              />
              <NewAppRuntimesLoader category="backend">
                {(items) => (
                  <RuntimePicker.Element
                    items={items}
                    value={inputProps.value.runtimePickerValue || {}}
                    onChange={(runtimePickerValue) => inputProps.onChange({...inputProps.value, runtimePickerValue})}
                  />
                )}
              </NewAppRuntimesLoader>
              {inputProps.value.runtimePickerValue && RuntimePicker.checkCompletion(inputProps.value.runtimePickerValue) && (
                <React.Fragment>
                  <Separator/>
                  <DescriptiveHeader
                    title="Capabilities"
                    description="Capabilities specify what your application can do.
     Select from the below, and we'll wire your application code,
     services, and OpenShift together end-to-end. When done, our friendly Welcome Application will show you how
     everything works."
                  />
                  <NewAppCapabilitiesLoader categories={['backend']} runtime={inputProps.value.runtimePickerValue.runtimeId}>
                    {(capabilities) => (
                      <CapabilitiesPicker.Element
                        items={capabilities.map(capabilityToItem)}
                        value={inputProps.value.capabilitiesPickerValue || {}}
                        onChange={(capabilitiesPickerValue) => inputProps.onChange({...inputProps.value, capabilitiesPickerValue})}
                      />
                    )}
                  </NewAppCapabilitiesLoader>
                </React.Fragment>
              )}
            </React.Fragment>
          )}
      </FormPanel>
    );
  }
};
