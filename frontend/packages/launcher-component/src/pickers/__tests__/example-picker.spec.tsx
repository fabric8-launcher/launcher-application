import * as React from 'react';
import { cleanup, render, fireEvent } from 'react-testing-library';
import { ExamplePicker } from '../example-picker';
import { ExampleMission } from 'launcher-client';
import { FormPanel } from '../../core/form-panel/form-panel';

afterEach(cleanup);

describe('<ExamplePicker />', () => {
  it('renders the ExamplePicker correctly', () => {
    const comp = render(<ExamplePicker.Element missions={missions} value={{}} onChange={() => {}}/>);
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('show the version dropdown', () => {
    const handleSave = jest.fn();
    const comp = render(
      <FormPanel
        initialValue={{}}
        validator={ExamplePicker.checkCompletion}
        onSave={handleSave}
        onCancel={() => {}}
      >
        {(inputProps) => (<ExamplePicker.Element missions={missions} {...inputProps}/>)}
      </FormPanel>
    );
    const missionRadio = comp.getByLabelText('Choose circuit-breaker as mission');
    fireEvent.click(missionRadio);
    expect(comp.getByLabelText('Select Runtime')).toBeDefined();
    fireEvent.change(comp.getByLabelText('Select Runtime'), { target: { value: 'nodejs' } });
    expect(comp.getByDisplayValue('10.x (Community)')).toBeDefined();
    fireEvent.change(comp.getByLabelText('Select Version'), { target: { value: 'community' } });
    fireEvent.click(comp.getByText('Save'));
    expect(handleSave).toHaveBeenCalledTimes(1);
    expect(handleSave.mock.calls[0][0]).toMatchSnapshot('Saved value');
    expect(comp.asFragment()).toMatchSnapshot('Component');
  });

  it('should not be downloadOnly with runsOn "local"', () => {
    downloadOnlyTest(['local']);
  });

  it('should not be downloadOnly with runsOn "!starter"', () => {
    downloadOnlyTest(['!starter']);
  });

  it('should be downloadOnly for runsOn "none"', () => {
    downloadOnlyTest('none');
  });
});

const downloadOnlyTest = runsOn => {
  const handleSave = jest.fn();
  const comp = render(
    <FormPanel
      initialValue={{}}
      validator={ExamplePicker.checkCompletion}
      onSave={handleSave}
      onCancel={() => {}}
    >
      {(inputProps) => (<ExamplePicker.Element missions={missions} {...inputProps}/>)}
    </FormPanel>
  );
  missions[0].runtime![0].versions[0].metadata.runsOn = runsOn;
  const missionRadio = comp.getByLabelText('Choose circuit-breaker as mission');
  fireEvent.click(missionRadio);
  fireEvent.change(comp.getByLabelText('Select Runtime'), { target: { value: 'nodejs' } });
  fireEvent.change(comp.getByLabelText('Select Version'), { target: { value: 'community' } });
  fireEvent.click(comp.getByText('Save'));
  expect(handleSave).toHaveBeenCalledTimes(1);
  expect(handleSave.mock.calls[0][0]).toMatchSnapshot(`Value with "${runsOn}"`);
};

const missions: ExampleMission[] = [
  {id: 'circuit-breaker', description: 'circuit-breaker description', name: 'Circuit Breaker', metadata: {}, runtime: [
    {id: 'nodejs', name: 'Node', icon: '', versions: [
      {id: 'community', name: '10.x (Community)', metadata: { runsOn: 'openshift'}}
    ]}
  ]}
];
