import '@testing-library/jest-dom/extend-expect';
import { cleanup, fireEvent, render, RenderResult } from '@testing-library/react';
import * as React from 'react';
import { mockLauncherClient } from '../../client/launcher.client.factory';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { CreateNewAppFlow } from '../create-new-app-flow';
import { downloadCheckPayload, launchCheckPayloadAndProgress, mockClientPromise } from './flow-helpers';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

mockClientPromise();

describe('<CreateNewAppFlow />', () => {
  it('renders and initializes the CreateNewAppFlow correctly', async () => {
    const comp = render(<LauncherDepsProvider><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);
    expect(comp.getByLabelText('Loading dest-repository')).toBeDefined();
    expect(comp.getByLabelText('Loading openshift-deployment')).toBeDefined();

    await checkInitialStatus(comp);
  });
  it('Configure backend and check full launch until next steps', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureBackend(comp, 'vertx', 'rest');

    expect(comp.getByLabelText('Launch Application')).not.toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).not.toHaveAttribute('disabled');

    await launchCheckPayloadAndProgress(comp, mockClient);

    expect(comp.getByLabelText('Welcome Application link').getAttribute('href')).toMatchSnapshot('Welcome Application link');

    expect(comp.getByLabelText('Repository link').getAttribute('href')).toMatchSnapshot('Repository link');

  });
  it('Configure frontend, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureFrontend(comp, 'react');

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');

  });

  it('Configure frontend and backend, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureFrontend(comp, 'react');

    await configureBackend(comp, 'vertx', 'rest');

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend with multiple capabilities, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureBackend(comp, 'quarkus', 'rest', 'database');

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend with no capability, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureBackend(comp, 'quarkus');

    fireEvent.change(comp.getByLabelText('Application Project name'), { target: { value: 'new-application-name' } });

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend and check full download until next steps', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureBackend(comp, 'quarkus', 'rest', 'database');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    await downloadCheckPayload(comp, mockClient);
  });

  it('Check that launch is working after download for the same application', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureBackend(comp, 'quarkus', 'rest', 'database');

    fireEvent.click(comp.getByLabelText('Download Application'));

    await comp.findByLabelText('Your Application is ready to be downloaded');

    fireEvent.click(comp.getByLabelText('Close'));
    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Check that welcome app can be unselected', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    fireEvent.click(comp.getByLabelText('Open welcome-app editor'));
    fireEvent.click(comp.getByLabelText('welcome app check'));

    fireEvent.click(comp.getByLabelText('Save welcome-app'));

    expect(comp.getAllByText('Welcome Application is disabled')).toBeDefined();
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('check that cancel is working correctly', async () => {
    const onCancel = jest.fn();
    const comp = render(<LauncherDepsProvider><CreateNewAppFlow appName="my-test-app" onCancel={onCancel} /></LauncherDepsProvider>);

    fireEvent.click(comp.getByLabelText('Cancel'));
    expect(onCancel).toHaveBeenCalled();

    await checkInitialStatus(comp);
  });
});

async function configureBackend(comp: RenderResult, runtime, ...capabilities: string[]) {
  fireEvent.click(comp.getByLabelText('Open backend editor'));
  await comp.findByLabelText('Edit backend');

  const selectRuntime = await comp.findByLabelText(`Select Runtime`);
  fireEvent.change(selectRuntime, { target: { value: runtime } });

  await comp.findByLabelText(`Select capability`);
  capabilities.forEach(c => fireEvent.click(comp.getByLabelText(`Pick ${c} capability`)));

  fireEvent.click(comp.getByLabelText('Save backend'));
  await comp.findByLabelText('backend is configured');
}

async function configureFrontend(comp: RenderResult, runtime) {
  fireEvent.click(comp.getByLabelText('Open frontend editor'));

  expect(comp.getByLabelText('Edit frontend')).toBeDefined();

  const selectRuntime = await comp.findByLabelText(`Choose ${runtime} as runtime`);
  fireEvent.click(selectRuntime);

  fireEvent.click(comp.getByLabelText('Save frontend'));
  await comp.findByLabelText('frontend is configured');
}

async function checkInitialStatus(comp) {

  expect(comp.getByLabelText('backend is not configured')).toBeDefined();
  expect(comp.getByLabelText('frontend is not configured')).toBeDefined();

  expect(comp.getByLabelText('Launch Application')).toHaveAttribute('disabled');
  expect(comp.getByLabelText('Download Application')).toHaveAttribute('disabled');

  await comp.findByLabelText('dest-repository is configured');
  await comp.findByLabelText('openshift-deployment is configured');
}
