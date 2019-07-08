import * as React from 'react';
import { fireEvent, render, act, cleanup } from '@testing-library/react';
import { newMockHealthChecksCapabilityApi } from '../HealthChecksCapabilityApi';
import { HealthChecksCapability, HealthChecksApiContext } from '../HealthChecksCapability';
import moment from 'moment-timezone';

jest.useFakeTimers();

moment.tz.setDefault('UTC');

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

describe('<HealthChecksCapability />', () => {
  it('check that initial render is correct', () => {
    const component = render(<HealthChecksCapability />);
    expect(component.asFragment()).toMatchSnapshot();
  });

  it('check that readiness is working', async () => {
    const api = newMockHealthChecksCapabilityApi();
    const result = { content: 'OK', time: 1542793377 };
    const spy = jest.spyOn(api, 'doGetReadiness').mockResolvedValue(result);
    const Wrapper: React.FunctionComponent = (props) => (<HealthChecksApiContext.Provider value={api}>{props.children}</HealthChecksApiContext.Provider>)
    const component = render(<HealthChecksCapability />, { wrapper: Wrapper });
    fireEvent.click(component.getByLabelText('Execute Readiness check'));
    await act(async () => {
      await spy.mock.results[0];
    });
    expect(spy).toHaveBeenCalled();
    expect(component.getByLabelText(result.content));
    expect(component.asFragment()).toMatchSnapshot();
  });

  it('check that liveness is working', async () => {
    const api = newMockHealthChecksCapabilityApi();
    const result = { content: 'OK', time: 1542793377 };
    const spy = jest.spyOn(api, 'doGetLiveness').mockResolvedValue(result);
    const Wrapper: React.FunctionComponent = (props) => (<HealthChecksApiContext.Provider value={api}>{props.children}</HealthChecksApiContext.Provider>)
    const component = render(<HealthChecksCapability />, { wrapper: Wrapper });
    fireEvent.click(component.getByLabelText('Execute Liveness check'));
    await act(async () => {
      await spy.mock.results[0];
    });
    expect(spy).toHaveBeenCalled();
    expect(component.getByLabelText(result.content));
    expect(component.asFragment()).toMatchSnapshot();
  });
});
