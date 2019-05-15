import { Capability, mockLauncherClient } from 'launcher-client';
import * as React from 'react';
import { cleanup, render } from 'react-testing-library';
import { LauncherDepsProvider } from '../../contexts/launcher-client-provider';
import { NewAppCapabilitiesLoader, getCapabilityRuntimeNameProp } from '../new-app-capabilities-loader';
import { flushPromises } from '../../core/__tests__/test-helpers';

afterEach(cleanup);

jest.useFakeTimers();

describe('<NewAppCapabilitiesLoader />', () => {
  it('should return corresponding capabilities depending on the selected categories', async () => {
    // when we ask for all the backend capabilities
    const mockContent = jest.fn();
    mockContent.mockReturnValue((<div aria-label="Capabilities are loaded">capabilities</div>));
    const comp = render((
      <NewAppCapabilitiesLoader categories={['backend']}>
        {(capabilities) => mockContent(capabilities)}
      </NewAppCapabilitiesLoader>
    ), { wrapper: LauncherDepsProvider as React.FunctionComponent<{}> });

    // then we get all the backend capabilities
    await flushPromises();
    expect(comp.getByLabelText('Capabilities are loaded')).toBeDefined();
    expect(mockContent).toHaveBeenCalledTimes(1);
    const result = mockContent.mock.calls[0][0] as Capability[];
    expect(result.filter(c => c.metadata.category === 'backend')).toHaveLength(result.length);
    expect(result).toHaveLength(3);
  });
  it('should return corresponding capabilities depending on the selected runtime', async () => {
    // given all capabilities are compatible with dotnet, only database is compatible with vertx
    const ClientWrapper: React.FunctionComponent<{}> = (props) => (
      <LauncherDepsProvider client={client}>{props.children}</LauncherDepsProvider>
    );
    const mockContent = jest.fn();
    mockContent.mockReturnValue((<div aria-label="Capabilities are loaded">capabilities</div>));
    const unchangedClient = mockLauncherClient();
    const client = mockLauncherClient();
    const spy = jest.spyOn(client, 'capabilities').mockImplementation(async () => {
      const caps = await unchangedClient.capabilities();
      return caps.map(c => {
        if (c.metadata.category === 'backend' && c.module !== 'database') {
          getCapabilityRuntimeNameProp(c).values = ['dotnet'];
        }
        return c;
      });
    });
    const categories = ['backend'];

    // when selected runtime is vertx
    const comp = render((
      <NewAppCapabilitiesLoader categories={categories} runtime="vertx">
        {(capabilities) => mockContent(capabilities)}
      </NewAppCapabilitiesLoader>
    ), { wrapper: ClientWrapper });

    // then we have only the database capability
    expect(comp.getByLabelText('Loading data')).toBeDefined();
    await flushPromises();
    expect(comp.getByLabelText('Capabilities are loaded')).toBeDefined();
    expect(spy).toHaveBeenCalledTimes(1);
    const result = mockContent.mock.calls[0][0] as Capability[];
    expect(result).toHaveLength(1);
    expect(result[0].module).toBe('database');

    // when selected runtime change to dotnet
    comp.rerender(
      <NewAppCapabilitiesLoader categories={categories} runtime="dotnet">
        {(capabilities) => mockContent(capabilities)}
      </NewAppCapabilitiesLoader>
    );

    // then all the capabilities are returned
    expect(comp.getByLabelText('Loading data')).toBeDefined();
    await flushPromises();
    expect(comp.getByLabelText('Capabilities are loaded')).toBeDefined();
    expect(spy).toHaveBeenCalledTimes(2);
    expect(mockContent).toHaveBeenCalledTimes(3); // there is one more cycle with prev value when deps are updated
    const resultAfterUpdate = mockContent.mock.calls[2][0] as Capability[];
    expect(resultAfterUpdate).toHaveLength(3);
  });
});
