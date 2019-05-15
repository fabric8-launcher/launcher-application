import 'jest-dom/extend-expect';
import { mockLauncherClient } from 'launcher-client';
import * as React from 'react';
import { cleanup, fireEvent, render } from 'react-testing-library';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { DeployExampleAppFlow } from '../deploy-example-app-flow';
import { launchCheckPayloadAndProgress } from './flow-helpers';
import { flushPromises } from '../../core/__tests__/test-helpers';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

jest.useFakeTimers();

async function chooseExample(comp, mission, runtime, version) {
  fireEvent.click(comp.getByLabelText('Open example editor'));

  expect(comp.getByLabelText('Edit example')).toBeDefined();

  // Resolve catalog
  await flushPromises();

  fireEvent.click(comp.getByLabelText(`Choose ${mission} as mission`));

  fireEvent.change(comp.getByLabelText('Select Runtime'), { target: { value: runtime } });
  fireEvent.change(comp.getByLabelText('Select Version'), { target: { value: version } });

  fireEvent.click(comp.getByLabelText('Save example'));

  // Resolve overview promises
  await flushPromises();
}

describe('<DeployExampleAppFlow />', () => {
  it('renders and initializes the DeployExampleAppFlow correctly', async () => {
    const comp = render(<LauncherDepsProvider><DeployExampleAppFlow appName="my-test-app"/></LauncherDepsProvider>);
    expect(comp.getByLabelText('Loading dest-repository')).toBeDefined();
    expect(comp.getByLabelText('Loading openshift-deployment')).toBeDefined();

    // Resolve data from auto loader
    await flushPromises();

    // Resolve overview promises
    await flushPromises();

    expect(comp.getByLabelText('dest-repository is configured')).toBeDefined();
    expect(comp.getByLabelText('openshift-deployment is configured')).toBeDefined();

    expect(comp.getByLabelText('example is not configured')).toBeDefined();

    expect(comp.getByLabelText('Launch Application')).toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).toHaveAttribute('disabled');
  });
  it('Choose example backend and check full launch until next steps popup', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><DeployExampleAppFlow appName="my-test-app"/></LauncherDepsProvider>);
    expect(comp.getByLabelText('Loading dest-repository')).toBeDefined();
    expect(comp.getByLabelText('Loading openshift-deployment')).toBeDefined();

    // Resolve data from auto loader
    await flushPromises();

    // Resolve overview promises
    await flushPromises();

    await chooseExample(comp, 'circuit-breaker', 'vert.x', 'redhat');
    expect(comp.getByLabelText('example is configured')).toBeDefined();

    fireEvent.change(comp.getByLabelText('Application Project name'), { target: { value: 'deploy-example-name' } });
    await launchCheckPayloadAndProgress(comp, mockClient);

    expect(comp.getByLabelText('Repository link').getAttribute('href')).toMatchSnapshot('Repository link');
  });
});
