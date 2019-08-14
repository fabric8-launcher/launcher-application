import '@testing-library/jest-dom/extend-expect';
import * as React from 'react';
import { cleanup, fireEvent, render } from '@testing-library/react';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { DeployExampleAppFlow } from '../deploy-example-app-flow';
import { launchCheckPayloadAndProgress, mockClientPromise } from './flow-helpers';
import { mockLauncherClient } from '../../client/launcher.client.factory';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

mockClientPromise();

async function chooseExample(comp, mission, runtime, version) {
  fireEvent.click(comp.getByLabelText('Open example editor'));

  expect(comp.getByLabelText('Edit example')).toBeDefined();

  const chooseMission = await comp.findByLabelText(`Choose ${mission} as mission`);
  fireEvent.click(chooseMission);

  await comp.findByLabelText('Select Runtime');

  fireEvent.change(comp.getByLabelText('Select Runtime'), { target: { value: runtime } });

  await comp.findByLabelText('Select Version');
  fireEvent.change(comp.getByLabelText('Select Version'), { target: { value: version } });

  fireEvent.click(comp.getByLabelText('Save example'));
  await comp.findByLabelText('example is configured');

}

describe('<DeployExampleAppFlow />', () => {
  it('renders and initializes the DeployExampleAppFlow correctly', async () => {
    const comp = render(<LauncherDepsProvider><DeployExampleAppFlow appName="my-test-app"/></LauncherDepsProvider>);
    expect(comp.getByLabelText('Loading dest-repository')).toBeDefined();
    expect(comp.getByLabelText('Loading openshift-deployment')).toBeDefined();
    expect(comp.getByLabelText('example is not configured')).toBeDefined();

    await comp.findByLabelText('dest-repository is configured');
    await comp.findByLabelText('openshift-deployment is configured');
    expect(comp.getByLabelText('Launch Application')).toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).toHaveAttribute('disabled');
  });
  it('Choose example backend and check full launch until next steps popup', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><DeployExampleAppFlow appName="my-test-app"/></LauncherDepsProvider>);

    await chooseExample(comp, 'circuit-breaker', 'vert.x', 'redhat');
   
    fireEvent.change(comp.getByLabelText('Application Project name'), { target: { value: 'deploy-example-name' } });
    await launchCheckPayloadAndProgress(comp, mockClient);

    expect(comp.getByLabelText('Repository link').getAttribute('href')).toMatchSnapshot('Repository link');
  });
});
