import React from 'react';
import { render, fireEvent } from 'react-testing-library';

import { NodeJSSettingsPicker } from '../nodejs-settings-picker';
import { FormPanel } from '../../core/form-panel/form-panel';

describe('<NodeJSSettingsPicker />', () => {
  it('renders the NodeJSSettingsPicker correctly', () => {
    const comp = render(<NodeJSSettingsPicker.Element value={{name: '', version: ''}} onChange={() => {}}/>);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show error for invalid data', () => {
    const handleSave = jest.fn();
    const comp = render(
      <FormPanel
        initialValue={{}}
        validator={NodeJSSettingsPicker.checkCompletion}
        onSave={handleSave}
        onCancel={() => {}}
      >
        {(inputProps) => (<NodeJSSettingsPicker.Element {...inputProps}/>)}
      </FormPanel>
    );

    const nameField = comp.getByLabelText('Nodejs package name');
    fireEvent.change(nameField, { target: { value: 'invalid name' } });
    const versionField = comp.getByLabelText('Nodejs version');
    fireEvent.change(versionField, { target: { value: '1' } });
    expect(comp.asFragment()).toMatchSnapshot();
  });
});
