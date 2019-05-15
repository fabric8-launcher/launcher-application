import * as React from 'react';
import {
  DataList,
  DataListCell,
  DataListContent,
  DataListItem,
  Radio,
  Title,
  FormSelect,
  FormSelectOption,
  Button,
  Split,
  SplitItem
} from '@patternfly/react-core';
import { ExampleRuntime, ExampleMission } from 'launcher-client';
import { InputProps, Picker } from '../core/types';
import _ from 'lodash';

export interface ExamplePickerValue {
  missions?: ExampleMission[];
  missionId?: string;
  runtimeId?: string;
  versionId?: string;
  downloadOnly?: boolean;
}

interface ExamplePickerProps extends InputProps<ExamplePickerValue> {
  missions?: ExampleMission[];
}

function isDownloadOnly(version) {
  return (!!version && (version.metadata.runsOn.indexOf('none') !== -1)) || false;
}

export const ExamplePicker: Picker<ExamplePickerProps, ExamplePickerValue> = {
  checkCompletion: value => !!value.missionId && !!value.runtimeId && !!value.versionId,
  Element: props => {
    // @ts-ignore
    const runtimesMap = _.keyBy(_.flatten(_.map(props.missions, 'runtime')), 'id');
    const missions = !props.value.missions ? props.missions : props.value.missions;
    const filterCatalog = (runtime) => {
      const filteredMissions = _.cloneDeep(props.missions);
      filteredMissions!.map(m => m.runtime = (m.runtime as ExampleRuntime[]).filter(r => r.id === runtime));
      props.onChange({
        runtimeId: runtime,
        missions: filteredMissions!.filter(m => (m.runtime as ExampleRuntime[]).find(r => !!r))
      });
    };
    return (
      <React.Fragment>
        <Split>
          <SplitItem isMain>
            Already decided what runtime you want to use? Select it here:
          </SplitItem>
          <SplitItem isMain={false}>
            <Button variant="tertiary" onClick={() => props.onChange({...props.value, missions: undefined, runtimeId: ''})}>Any</Button>
            {_.map(runtimesMap).map((runtime, index) => (
              <Button
                style={{marginLeft: '5px'}}
                variant="tertiary"
                key={index}
                onClick={() => filterCatalog(runtime.id)}
                isDisabled={props.value.runtimeId === runtime.id && !!props.value.missions}
              >
                {runtime.name}
              </Button>
            ))}
          </SplitItem>
        </Split>
        <DataList aria-label="Choose an example" style={{marginTop: '20px'}}>
          {
            missions!.map((mission, i) => {
              const isSelected = props.value.missionId === mission.id;
              const onChange = (runtimeId = props.value.runtimeId, versionId?) => {
                const runtime = (mission.runtime! as any).find(r => r.id === runtimeId);
                if (runtimeId) {
                  if (!runtime || !runtime.versions || runtime.versions.lenght <= 0) {
                    runtimeId = undefined;
                  } else if(!versionId) {
                    versionId = runtime.versions[0].id;
                  }
                }
                const version = runtime && runtime.versions.find(v => v.id === versionId);
                props.onChange({
                  ...props.value,
                  missionId: mission.id,
                  runtimeId,
                  versionId,
                  downloadOnly: isDownloadOnly(version)
                });
              };
              const selectedRuntime = (mission.runtime! as any).find(r => r.id === props.value.runtimeId);
              return (
                <DataListItem
                  isExpanded={isSelected}
                  aria-labelledby={mission.name}
                  value={mission.id}
                  key={i}
                  style={{cursor: 'pointer'}}
                >
                  <DataListCell width={1} style={{flex: 'none'}}>
                    <Radio
                      aria-label={`Choose ${mission.id} as mission`}
                      value={mission.id}
                      checked={isSelected}
                      onChange={() => onChange()}
                      name="mission"
                      id={`radio-choose-${mission.id}-as-mission`}
                    />
                  </DataListCell>
                  <DataListCell width={1} onClick={() => onChange()}><Title size="lg">{mission.name}</Title></DataListCell>
                  <DataListCell width={2} onClick={() => onChange()}>{mission.description}</DataListCell>
                  <DataListContent aria-label={`Detail for ${mission.id}`} isHidden={!isSelected}>
                    <FormSelect
                      isDisabled={mission.runtime!.length === 1}
                      id={`${mission.id}-runtime-select`}
                      value={props.value.runtimeId || ''}
                      onChange={r => onChange(r)}
                      aria-label="Select Runtime"
                    >
                      {mission.runtime!.length !== 1 && <FormSelectOption
                          value=""
                          label="Select a Runtime"
                      />}
                      {mission.runtime!.map((runtime, index) => (
                        <FormSelectOption
                          key={index}
                          value={runtime.id}
                          label={runtime.name}
                        />
                      ))
                      }
                    </FormSelect>
                    {!!selectedRuntime &&
                    <FormSelect
                        id={`${mission.id}-version-select`}
                        value={props.value.versionId}
                        onChange={value => onChange(selectedRuntime.id, value)}
                        aria-label="Select Version"
                    >
                      {selectedRuntime.versions.map((version, index) => (
                        <FormSelectOption
                          key={index}
                          value={version.id}
                          label={version.name}
                        />
                      ))
                      }
                    </FormSelect>
                    }
                  </DataListContent>
                </DataListItem>
              );
            })
          }
        </DataList>
      </React.Fragment>
    );
  }

};
