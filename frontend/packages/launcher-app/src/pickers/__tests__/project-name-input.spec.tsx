import React from 'react';
import { render, fireEvent, cleanup } from '@testing-library/react';

import { ProjectNameInput } from '../project-name-input';
import { mockLauncherClient } from '../../client/launcher.client.factory';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { flushPromises } from '../../__tests__/test-helpers';

describe('<ProjectNameInput />', () => {
  const mockClient = mockLauncherClient();
  afterEach(() => {
    cleanup();
  });

  jest.useFakeTimers();

  it('renders the ProjectNameInput correctly', () => {
    const comp = render(
      <LauncherDepsProvider client={mockClient}>
        <ProjectNameInput prefix="New Application:" />
      </LauncherDepsProvider>
    );
    expect(comp.asFragment()).toMatchSnapshot();
  });

  it('renders the warning if the project name already exists', async () => {
    const comp = render(
      <LauncherDepsProvider client={mockClient}>
        <ProjectNameInput prefix="New Application:" value="my-project" />
      </LauncherDepsProvider>
    );
    const input = comp.getByLabelText('Application Project name');
    fireEvent.blur(input);
    await flushPromises();

    expect(comp.getByText('Warning this project exists! Make sure you have write access.')).toBeDefined();
    expect(comp.asFragment()).toMatchSnapshot();
  });
});
