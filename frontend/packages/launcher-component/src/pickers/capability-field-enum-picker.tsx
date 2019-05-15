import { Form, FormGroup, Radio } from '@patternfly/react-core';
import * as React from 'react';

interface FieldEnumProps {
  parent: string;
  id: string;
  name: string;
  description: string;
  values: Array<{ id: string, name: string }>;
  required: boolean;
  value: string;

  onChange?(selected: string);
}

export function CapabilityFieldEnumPicker(props: FieldEnumProps) {
  const onChange = (_, event) => {
    if (props.onChange) {
      props.onChange(event.currentTarget.value);
    }
  };
  return (
    <Form isHorizontal>
      <FormGroup
        label={props.description}
        isRequired={props.required}
        fieldId={`capability-prop-${props.parent}-${props.id}`}
      >
        {props.values.map(v => (
          <Radio
            key={v.id}
            label={v.name}
            aria-label={`Select ${v} as ${props.name}`}
            checked={props.value === v.id}
            name={'select-' + props.id}
            id={`select-${props.parent}-${props.id}`}
            value={v.id}
            onChange={onChange}
          />
        ))}
      </FormGroup>
    </Form>
  );
}
