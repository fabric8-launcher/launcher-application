import '@testing-library/jest-dom/extend-expect';
import { cleanup, fireEvent, render } from '@testing-library/react';
import * as React from 'react';
import { mockLauncherClient } from '../../client/launcher.client.factory';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { ImportExistingFlow } from '../import-existing-flow';
import { launchCheckPayloadAndProgress, mockClientPromise } from './flow-helpers';

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

mockClientPromise();

async function configureSrc(comp, url) {
  fireEvent.click(comp.getByLabelText('Open src-repository editor'));
  expect(comp.getByLabelText('Edit src-repository')).toBeDefined();

  fireEvent.change(comp.getByLabelText('Git repository url'), { target: { value: url } });
  fireEvent.click(comp.getByText('Done'));

  const advancedSettings = await comp.findByText('Show Advanced Settings');
  fireEvent.click(advancedSettings);
  expect(comp.getByLabelText('select-buildImage')).toBeDefined();

  fireEvent.click(comp.getByLabelText('Choose Java Code Builder'));

  fireEvent.change(comp.getByPlaceholderText('Type the environment variable name'), {target: {value: 'JAVA_DEBUG'}});
  fireEvent.change(comp.getByPlaceholderText('Type the environment variable value'), {target: {value: 'true'}});
  fireEvent.click(comp.getByLabelText('Save src-repository'));

}

describe('<ImportExistingFlow />', () => {
  it('renders and initializes the ImportExistingFlow correctly', async () => {
    const comp = render(<LauncherDepsProvider><ImportExistingFlow appName="my-test-app" /></LauncherDepsProvider>);
    expect(comp.getByLabelText('openshift-deployment is not configured')).toBeDefined();

    await comp.findByLabelText('openshift-deployment is configured');

    expect(comp.getByLabelText('Launch Application')).toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).toHaveAttribute('disabled');
  });

  it('Configure source repository to import and check full launch until next steps popup', async () => {
    const mockClient = mockLauncherClient();
    const comp = render(<LauncherDepsProvider client={mockClient}><ImportExistingFlow appName="my-test-app" /></LauncherDepsProvider>);

    await configureSrc(comp, 'https://github.com/nodeshift-starters/nodejs-rest-http');
    expect(comp.getByText('Import is configured')).toBeDefined();

    expect(comp.getByLabelText('Launch Application')).not.toHaveAttribute('disabled');
    expect(comp.getByLabelText('Download Application')).not.toHaveAttribute('disabled');

    await launchCheckPayloadAndProgress(comp, mockClient);
  });
});
