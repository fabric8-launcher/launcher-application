import { fireEvent } from 'react-testing-library';
import { flushPromises } from '../../core/__tests__/test-helpers';

export async function launchCheckPayloadAndProgress(comp, mockClient) {
  fireEvent.click(comp.getByLabelText('Launch Application'));

  expect(comp.getByLabelText('Waiting for server response...')).toBeDefined();

  expect(mockClient.currentPayload).toMatchSnapshot('payload');

  // Resolve launch result
  await flushPromises();

  expect(comp.getByLabelText('Receiving launch progress events...')).toBeDefined();
  expect(comp.getByLabelText('GITHUB_CREATE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('GITHUB_PUSHED is in-progress')).toBeDefined();
  expect(comp.getByLabelText('OPENSHIFT_CREATE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('OPENSHIFT_PIPELINE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('GITHUB_WEBHOOK is in-progress')).toBeDefined();

  // Resolve GITHUB_CREATE
  await flushPromises();

  expect(comp.getByLabelText('GITHUB_CREATE is completed')).toBeDefined();

  // Resolve GITHUB_PUSHED
  await flushPromises();
  expect(comp.getByLabelText('GITHUB_PUSHED is completed')).toBeDefined();

  // Resolve OPENSHIFT_CREATE
  await flushPromises();
  expect(comp.getByLabelText('OPENSHIFT_CREATE is completed')).toBeDefined();

  // Resolve OPENSHIFT_PIPELINE
  await flushPromises();
  expect(comp.getByLabelText('OPENSHIFT_PIPELINE is completed')).toBeDefined();

  // Resolve GITHUB_WEBHOOK
  await flushPromises();
  expect(comp.getByLabelText('GITHUB_WEBHOOK is completed')).toBeDefined();

  // Resolve complete progress
  await flushPromises();

  expect(comp.getByLabelText('Your Application has been launched')).toBeDefined();
  expect(comp.getByLabelText('Start a new Application')).toBeDefined();
  expect(comp.getByLabelText('Console link').getAttribute('href')).toMatchSnapshot('Console link');
}

export async function downloadCheckPayload(comp, mockClient) {
  fireEvent.click(comp.getByLabelText('Download Application'));

  expect(comp.getByLabelText('Waiting for server response...')).toBeDefined();

  expect(mockClient.currentPayload).toMatchSnapshot('payload');

  // Resolve download result
  await flushPromises();

  expect(comp.getByLabelText('Your Application is ready to be downloaded')).toBeDefined();

  expect(comp.getByLabelText('Download link').getAttribute('href')).toMatchSnapshot('Download link');
  expect(comp.getByLabelText('Start a new Application')).toBeDefined();

}
