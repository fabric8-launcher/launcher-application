import { fireEvent, RenderResult } from '@testing-library/react';

export function mockClientPromise() {
  jest.mock('../../client/helpers/mock-helpers', () => {
    return {
      promiseWithDelay: (name) => {
        console.log(name)
        return Promise.resolve();
      }
    };
  });
}

export async function launchCheckPayloadAndProgress(comp: RenderResult, mockClient) {
  fireEvent.click(comp.getByLabelText('Launch Application'));
  
  expect(mockClient.currentPayload).toMatchSnapshot('payload');
  await comp.findByLabelText('Receiving launch progress events...');
  expect(comp.getByLabelText('GITHUB_CREATE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('GITHUB_PUSHED is in-progress')).toBeDefined();
  expect(comp.getByLabelText('OPENSHIFT_CREATE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('OPENSHIFT_PIPELINE is in-progress')).toBeDefined();
  expect(comp.getByLabelText('GITHUB_WEBHOOK is in-progress')).toBeDefined();
  await comp.findByLabelText('GITHUB_CREATE is completed');
  await comp.findByLabelText('GITHUB_PUSHED is completed');
  await comp.findByLabelText('OPENSHIFT_CREATE is completed');
  await comp.findByLabelText('OPENSHIFT_PIPELINE is completed');
  await comp.findByLabelText('GITHUB_WEBHOOK is completed');

  await comp.findByLabelText('Your Application has been launched');
  expect(comp.getByLabelText('Start a new Application')).toBeDefined();
  expect(comp.getByLabelText('Console link').getAttribute('href')).toMatchSnapshot('Console link');
}

export async function downloadCheckPayload(comp, mockClient) {
  fireEvent.click(comp.getByLabelText('Download Application'));

  await comp.findByLabelText('Waiting for server response...');

  expect(mockClient.currentPayload).toMatchSnapshot('payload');

  await comp.findByLabelText('Your Application is ready to be downloaded');
  expect(comp.getByLabelText('Download link').getAttribute('href')).toMatchSnapshot('Download link');
  expect(comp.getByLabelText('Start a new Application')).toBeDefined();

}
