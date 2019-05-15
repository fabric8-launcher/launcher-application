import React from 'react';
import { render, fireEvent } from 'react-testing-library';
import { FormPanel } from '../../core/form-panel/form-panel';

import { MavenSettingsPicker } from '../maven-settings-picker';

describe('<MavenSettingsPicker />', () => {
  it('renders the MavenSettingsPicker correctly', () => {
    const comp = render(<MavenSettingsPicker.Element value={{groupId: '', version: '', artifactId: ''}} onChange={() => {}}/>);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show error for invalid data', () => {
    const handleSave = jest.fn();
    const comp = render(
      <FormPanel
        initialValue={{}}
        validator={MavenSettingsPicker.checkCompletion}
        onSave={handleSave}
        onCancel={() => {}}
      >
        {(inputProps) => (<MavenSettingsPicker.Element {...inputProps}/>)}
      </FormPanel>
    );

    const groupIdField = comp.getByLabelText('Maven groupId name');
    fireEvent.change(groupIdField, { target: { value: 'invalid name' } });
    const artifactIdField = comp.getByLabelText('Maven artifactId name');
    fireEvent.change(artifactIdField, { target: { value: 'invalid name' } });
    const versionField = comp.getByLabelText('Maven version number');
    fireEvent.change(versionField, { target: { value: '1' } });
    expect(comp.asFragment()).toMatchSnapshot();
  });
});
