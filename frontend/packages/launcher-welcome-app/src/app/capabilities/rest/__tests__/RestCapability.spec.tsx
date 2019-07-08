import * as React from 'react';
import { act, fireEvent, render, cleanup } from '@testing-library/react';
import { RestCapability, RestCapabilityApiContext } from '../RestCapability';
import { newMockRestCapabilityApi } from '../RestCapabilityApi';
import moment from 'moment-timezone';

const extra = {
  sourceMapping: {
    greetingEndpoint: 'src/main/java/org/your/GreetingApi.java',
  },
};

moment.tz.setDefault('UTC');

jest.useFakeTimers();

beforeAll(() => {
  process.env.TZ = 'UTC';
})

afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

describe('<RestCapability />', () => {
  it('check that initial render is correct', () => {
    const component = render(<RestCapability sourceMapping={extra.sourceMapping} />);
    expect(component).toMatchSnapshot();
  });

  it('check that a click on the GET button adds a Hello World message in the console', async () => {
    const api = newMockRestCapabilityApi();
    const result = { content: 'Hello World!', time: 1542793377 };
    const spy = jest.spyOn(api, 'doGetGreeting').mockResolvedValue(result);
    const Wrapper: React.FunctionComponent = (props) => (<RestCapabilityApiContext.Provider value={api}>{props.children}</RestCapabilityApiContext.Provider>)
    const component = render(<RestCapability sourceMapping={extra.sourceMapping} />, { wrapper: Wrapper });
    fireEvent.click(component.getByLabelText('Execute GET Greetings'));
    await act(async () => {
      await spy.mock.results[0];
    });
    expect(spy).toHaveBeenCalledWith('');
    expect(component.getByLabelText(result.content));
    expect(component.asFragment()).toMatchSnapshot();
  });

  it('check that after typing a name, a click on the GET button adds a Hello John message in the console', async () => {
    const api = newMockRestCapabilityApi();
    const result = { content: 'Hello John!', time: 1542793377 };
    const spy = jest.spyOn(api, 'doGetGreeting').mockResolvedValue(result);
    const Wrapper: React.FunctionComponent = (props) => (<RestCapabilityApiContext.Provider value={api}>{props.children}</RestCapabilityApiContext.Provider>)
    const component = render(<RestCapability sourceMapping={extra.sourceMapping} />, { wrapper: Wrapper });
    fireEvent.change(component.getByLabelText('Greetings name input'), { target: { value: 'John' } });
    expect(component.getByLabelText('Greetings name input').getAttribute('value')).toBe('John');
    fireEvent.click(component.getByLabelText('Execute GET Greetings'));
    await act(async () => {
      await spy.mock.results[0];
    });
    expect(spy).toHaveBeenCalledWith('John');
    expect(component.getByLabelText(result.content));
    expect(component.asFragment()).toMatchSnapshot();
  });

});
