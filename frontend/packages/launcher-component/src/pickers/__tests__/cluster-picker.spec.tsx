import * as React from 'react';
import { cleanup, render, fireEvent } from 'react-testing-library';
import { ClusterPicker } from '../cluster-picker';
import { OpenShiftCluster } from 'launcher-client';

afterEach(cleanup);

describe('<ClusterPicker />', () => {
    let clusters: OpenShiftCluster[];
    beforeEach(() => {
        clusters = [
            {
                connected: false,
                id: 'starter-us-east-1',
                name: 'Starter: US East (Virginia)',
                type: 'starter',
            },
            {
                connected: false,
                id: 'starter-us-west-1',
                name: 'Starter: US West (California)',
                type: 'starter',
            },
            {
                connected: false,
                id: 'starter-us-west-2',
                name: 'Starter: US West (Oregon)',
                type: 'starter',
            },
            {
                connected: false,
                id: 'starter-ca-central-1',
                name: 'Starter: Canada (Central)',
                type: 'starter',
            },
            {
                connected: false,
                id: 'pro-us-east-1',
                name: 'Pro: US East (N. Virginia)',
                type: 'pro',
            },
            {
                connected: false,
                id: 'pro-eu-west-1',
                name: 'Pro: EU West (Ireland)',
                type: 'pro',
            },
            {
                connected: false,
                id: 'pro-ap-southeast-2',
                name: 'Pro: Asia Pacific (Sydney)',
                type: 'pro',
            }
        ];
    });
    it('should display "No Active Clusters Found" when cluster list is empty', () => {
        const onChange = jest.fn();
        const linkGenerator = jest.fn();
        const comp = render(
            <ClusterPicker.Element clusters={[]} value={{}} onChange={onChange} authorizationLinkGenerator={linkGenerator} />
        );
        expect(onChange).not.toHaveBeenCalled();
        expect(comp.asFragment()).toMatchSnapshot();
    });
    it('should display list of clusters and don\'t autoselect when not connected cluster is available', () => {
        const onChange = jest.fn();
        const linkGenerator = jest.fn();
        const comp = render(
            <ClusterPicker.Element clusters={clusters} value={{}} onChange={onChange} authorizationLinkGenerator={linkGenerator} />
        );
        expect(onChange).not.toHaveBeenCalled();
        expect(comp.asFragment()).toMatchSnapshot();
    });
    it('should display list of clusters and autoselect when at least one connected cluster is available', () => {
        clusters[3].connected = true;
        clusters[4].connected = true;
        const onChange = jest.fn();
        const linkGenerator = jest.fn();
        const comp = render(
            <ClusterPicker.Element clusters={clusters} value={{}} onChange={onChange} authorizationLinkGenerator={linkGenerator} />
        );
        expect(onChange).toHaveBeenCalledWith({ clusterId: 'starter-ca-central-1', clusterType: 'starter' });
        expect(comp.asFragment()).toMatchSnapshot();
    });
    it('should select the cluster when the user click on it', () => {
        const onChange = jest.fn();
        const linkGenerator = jest.fn();

        const comp = render(
            <ClusterPicker.Element
                clusters={clusters.map(c => ({ ...c, connected: true }))}
                value={{ clusterId: 'starter-ca-central-1', clusterType: 'starter' }}
                onChange={onChange}
                authorizationLinkGenerator={linkGenerator}
            />
        );
        fireEvent.click(comp.getByLabelText('Choose starter-us-west-1 as cluster'));
        expect(onChange).toHaveBeenCalledWith({ clusterId: 'starter-us-west-1', clusterType: 'starter' });
        expect(comp.asFragment()).toMatchSnapshot();
    });
});
