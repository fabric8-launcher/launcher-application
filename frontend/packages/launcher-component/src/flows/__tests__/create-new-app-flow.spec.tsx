import * as React from 'react';
import 'jest-dom/extend-expect';
import { cleanup, fireEvent, render } from 'react-testing-library';
import { CreateNewAppFlow } from '../create-new-app-flow';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { mockLauncherClient } from '@launcher/client';
import { downloadCheckPayload, launchCheckPayloadAndProgress } from './flow-helpers';
import { flushPromises } from '../../core/__tests__/test-helpers';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

jest.useFakeTimers();

describe('<CreateNewAppFlow />', () => {
  it('renders and initializes the CreateNewAppFlow correctly', async () => {
    const comp = render(<LauncherDepsProvider><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);
    expect(comp.getByLabelText('Loading dest-repository')).toBeDefined();
    expect(comp.getByLabelText('Loading openshift-deployment')).toBeDefined();

    // Resolve data from auto loader
    await flushPromises();

    // Resolve overview promises
    await flushPromises();

    checkInitialStatus(comp);
  });
  it('Configure backend and check full launch until next steps', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureBackend(comp, 'vertx', 'rest');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    expect(comp.getByLabelText('Launch Application')).not.toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).not.toHaveAttribute('disabled');

    await launchCheckPayloadAndProgress(comp, mockClient);

    expect(comp.getByLabelText('Welcome Application link').getAttribute('href')).toMatchSnapshot('Welcome Application link');

    expect(comp.getByLabelText('Repository link').getAttribute('href')).toMatchSnapshot('Repository link');
  });
  it('Configure frontend, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureFrontend(comp, 'react');
    expect(comp.getByLabelText('frontend is configured')).toBeDefined();

    fireEvent.click(comp.getByLabelText('Launch Application'));

    expect(mockClient.currentPayload).toMatchSnapshot('payload');

  });

  it('Configure frontend and backend, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureFrontend(comp, 'react');
    expect(comp.getByLabelText('frontend is configured')).toBeDefined();

    await configureBackend(comp, 'vertx', 'rest');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend with multiple capabilities, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureBackend(comp, 'quarkus', 'rest', 'database');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend with no capability, launch and check payload', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureBackend(comp, 'quarkus');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    fireEvent.change(comp.getByLabelText('Application Project name'), { target: { value: 'new-application-name' } });

    fireEvent.click(comp.getByLabelText('Launch Application'));
    expect(mockClient.currentPayload).toMatchSnapshot('payload');
  });

  it('Configure backend and check full download until next steps', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureBackend(comp, 'quarkus', 'rest', 'database');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    await downloadCheckPayload(comp, mockClient);
  });

  it('Check that launch is working after download for the same application', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><CreateNewAppFlow appName="my-test-app" /></LauncherDepsProvider>);

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    await configureBackend(comp, 'quarkus', 'rest', 'database');
    expect(comp.getByLabelText('backend is configured')).toBeDefined();

    fireEvent.click(comp.getByLabelText('Download Application'));
    expect(comp.getByLabelText('Waiting for server response...')).toBeDefined();

    // Resolve download result
    await flushPromises();
    expect(comp.getByLabelText('Your Application is ready to be downloaded')).toBeDefined();

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

    // Resolve data from auto loader
    await flushPromises();
    // Resolve overview promises
    await flushPromises();

    fireEvent.click(comp.getByLabelText('Cancel'));

    expect(onCancel).toHaveBeenCalled();

    // Resolve data from auto loader
    await flushPromises();

    // Resolve overview promises
    await flushPromises();

    checkInitialStatus(comp);
  });
});

async function configureBackend(comp, runtime, ...capabilities: string[]) {
  fireEvent.click(comp.getByLabelText('Open backend editor'));

  expect(comp.getByLabelText('Edit backend')).toBeDefined();

  // Resolve runtimes
  await flushPromises();

  fireEvent.change(comp.getByLabelText(`Select Runtime`), { target: { value: runtime } });

  // Resolve promises
  await flushPromises();

  capabilities.forEach(c => fireEvent.click(comp.getByLabelText(`Pick ${c} capability`)));

  fireEvent.click(comp.getByLabelText('Save backend'));

  // Resolve overview promises
  await flushPromises();
}

async function configureFrontend(comp, runtime) {
  fireEvent.click(comp.getByLabelText('Open frontend editor'));

  expect(comp.getByLabelText('Edit frontend')).toBeDefined();

  // Resolve runtimes
  await flushPromises();

  fireEvent.click(comp.getByLabelText(`Choose ${runtime} as runtime`));

  fireEvent.click(comp.getByLabelText('Save frontend'));

  // Resolve overview promises
  await flushPromises();
}

function checkInitialStatus(comp) {
  expect(comp.getByLabelText('dest-repository is configured')).toBeDefined();
  expect(comp.getByLabelText('openshift-deployment is configured')).toBeDefined();

  expect(comp.getByLabelText('backend is not configured')).toBeDefined();
  expect(comp.getByLabelText('frontend is not configured')).toBeDefined();

  expect(comp.getByLabelText('Launch Application')).toHaveAttribute('disabled');
  expect(comp.getByLabelText('Download Application')).toHaveAttribute('disabled');
}
