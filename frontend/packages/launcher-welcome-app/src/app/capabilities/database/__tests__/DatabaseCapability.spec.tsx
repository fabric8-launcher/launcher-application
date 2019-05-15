import * as React from 'react';
import { act, cleanup, fireEvent, render } from 'react-testing-library';
import { DatabaseCapability, DatabaseCapabilityApiContext } from '../DatabaseCapability';
import { MOCK_FRUITS, newMockDatabaseCapabilityApi } from '../DatabaseCapabilityApi';
import moment from 'moment-timezone';

const extra = {
    sourceMapping: {
        dbEndpoint: 'src/main/java/org/your/DbEndpoint.java',
    },
    sourceRepository: {
        url: 'https://www.github.com/ia3andy/toto',
        provider: 'github',
    },
};

jest.useFakeTimers();

moment.tz.setDefault('UTC');

afterEach(() => {
    console.log('cleanup()');
    cleanup();
});

describe('<DatabaseCapability />', () => {
    it('check that initial render is correct', () => {
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />);
        expect(component).toMatchSnapshot();
    });

    it('check that a click on the GET button list the fruits in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const result = { content: MOCK_FRUITS, time: 1542793377 };
        const spy = jest.spyOn(api, 'doFetchFruits').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.click(component.getByLabelText('Execute GET Fruits'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalled();
        expect(component.getByLabelText(JSON.stringify(MOCK_FRUITS)));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the POST button add a new fruit with default in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const fruit = { id: 4, name: 'Coco', stock: 10 };
        const result = { content: fruit, time: 1542793377 };
        const spy = jest.spyOn(api, 'doPostFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.click(component.getByLabelText('Execute POST Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith({ name: 'Coco', stock: 10 });
        expect(component.getByLabelText(JSON.stringify(fruit)));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the POST button add a new fruit in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const fruit = { id: 4, name: 'Passion', stock: 15 };
        const result = { content: fruit, time: 1542793377 };
        const spy = jest.spyOn(api, 'doPostFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.change(component.getByLabelText('Fruit to create'), { target: { value: fruit.name } });
        expect(component.getByLabelText('Fruit to create').getAttribute('value')).toBe(fruit.name);
        fireEvent.change(component.getByLabelText('Stock to create'), { target: { value: `${fruit.stock}` } });
        expect(component.getByLabelText('Stock to create').getAttribute('value')).toBe(`${fruit.stock}`);
        fireEvent.click(component.getByLabelText('Execute POST Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith({ name: 'Passion', stock: 15 });
        expect(component.getByLabelText(JSON.stringify(fruit)));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the PUT button update the default fruit in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const fruit = { id: 2, name: 'Banana', stock: 10 };
        const result = { content: fruit, time: 1542793377 };
        const spy = jest.spyOn(api, 'doPutFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.click(component.getByLabelText('Execute PUT Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith(2, { name: 'Banana', stock: 10 });
        expect(component.getByLabelText(JSON.stringify(fruit)));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the PUT button update the selected fruit in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const fruit = { id: 2, name: 'Passion', stock: 22 };
        const result = { content: fruit, time: 1542793377 };
        const spy = jest.spyOn(api, 'doPutFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.change(component.getByLabelText('Fruit ID to update'), { target: { value: `${fruit.id}` } });
        expect(component.getByLabelText('Fruit ID to update').getAttribute('value')).toBe(`${fruit.id}`);
        fireEvent.change(component.getByLabelText('New fruit name'), { target: { value: fruit.name } });
        expect(component.getByLabelText('New fruit name').getAttribute('value')).toBe(fruit.name);
        fireEvent.change(component.getByLabelText('New fruit stock'), { target: { value: `${fruit.stock}` } });
        expect(component.getByLabelText('New fruit stock').getAttribute('value')).toBe(`${fruit.stock}`);
        fireEvent.click(component.getByLabelText('Execute PUT Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith(2, { name: 'Passion', stock: 22 });
        expect(component.getByLabelText(JSON.stringify(fruit)));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the DELETE button delete the default fruit in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const result = { time: 1542793377 };
        const spy = jest.spyOn(api, 'doDeleteFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.click(component.getByLabelText('Execute DELETE Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith(2);
        expect(component.getByLabelText('OK'));
        expect(component.asFragment()).toMatchSnapshot();
    });
    it('check that a click on the DELETE button delete the selected fruit in the console', async () => {
        const api = newMockDatabaseCapabilityApi();
        const result = { time: 1542793377 };
        const spy = jest.spyOn(api, 'doDeleteFruit').mockResolvedValue(result);
        const Wrapper: React.FunctionComponent = (props) => (<DatabaseCapabilityApiContext.Provider value={api}>{props.children}</DatabaseCapabilityApiContext.Provider>)
        const component = render(<DatabaseCapability sourceMapping={extra.sourceMapping} sourceRepository={extra.sourceRepository} />, { wrapper: Wrapper });
        fireEvent.change(component.getByLabelText('Fruit ID to delete'), { target: { value: '3' } });
        expect(component.getByLabelText('Fruit ID to delete').getAttribute('value')).toBe('3');
        fireEvent.click(component.getByLabelText('Execute DELETE Fruit'));
        await act(async () => {
            await spy.mock.results[0];
        });
        expect(spy).toHaveBeenCalledWith(3);
        expect(component.getByLabelText('OK'));
        expect(component.asFragment()).toMatchSnapshot();
    });
});
