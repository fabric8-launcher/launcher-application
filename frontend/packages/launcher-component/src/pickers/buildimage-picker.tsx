import React, { Fragment } from 'react';
import { Alert, DataList, DataListCell, DataListItem, Radio, Title } from '@patternfly/react-core';
import { BuilderImage } from 'launcher-client';
import { InputProps, Picker } from '../core/types';
import { SpecialValue } from '../core/stuff';
import { TogglePanel } from '../core/toggle-panel/toggle-panel';

export interface BuildImagePickerValue {
  image?: string;
  advanced?: boolean;
}

interface BuildImageProps extends InputProps<BuildImagePickerValue> {
  suggestedImageName: string;
  builderImages: BuilderImage[];
}

export const BuildImagePicker: Picker<BuildImageProps, BuildImagePickerValue> = {
  checkCompletion: value => !!value.image,
  Element: props => {
    const imageName = props.builderImages.find(i => i.id == props.value.image)!.name;
    return (
      <Fragment>
        <p>
          For your codebase, our runtime detection algorithm suggests to use this builder image: <SpecialValue>{props.suggestedImageName}</SpecialValue>
          <br/>Currently selected: <SpecialValue>{imageName || props.suggestedImageName}</SpecialValue>
        </p>
        <TogglePanel title="Advanced settings">
          <div>
            <Alert variant="warning" title="Picking the wrong builder image may result in a failed deployment!" style={{ margin: '20px' }} />
            <DataList aria-label="select-buildImage">
              {props.builderImages.map((image, index) => {
                const isSelected = props.value.image === image.id;
                const onChangeSelected = () => {
                  props.onChange({ ...props.value, image: image.id });
                };

                return (
                  <DataListItem
                    aria-labelledby={image.name}
                    isExpanded={false}
                    key={index}
                    style={isSelected ? { borderLeft: '2px solid #007bba' } : {}}
                  >
                    <DataListCell width={1} style={{ flex: 'none' }}>
                      <Radio
                        aria-label={`Choose ${image.name}`}
                        value={image.id}
                        checked={isSelected}
                        onChange={onChangeSelected}
                        name="image"
                        id={`radio-choose-${image.id}`}
                      />
                    </DataListCell>
                    <DataListCell width={1} onClick={onChangeSelected} style={{ cursor: 'pointer' }}>
                      <Title size="lg">{image.name}</Title>
                    </DataListCell>
                    <DataListCell width={2} onClick={onChangeSelected} style={{ cursor: 'pointer' }}>
                      {image.id}
                    </DataListCell>
                  </DataListItem>
                );
              })
              }
            </DataList>
          </div>
        </TogglePanel>
      </Fragment>
    );
  }
};
