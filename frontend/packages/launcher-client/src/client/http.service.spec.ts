import { HttpService } from './http.service';
import axios from 'axios';
import MockAdaptor from 'axios-mock-adapter';
import { OpenShiftCluster } from './types';

describe('HttpService test', () => {
  const backendUrl = 'http://localhost:8082';

  const mock = new MockAdaptor(axios);
  const httpService = new HttpService();

  beforeAll(() => {
    mock.onGet(backendUrl + '/text').reply(200, 'response');
    mock.onGet(backendUrl + '/cluster').reply(200, [
      {
        connected: false,
        cluster: {
          id: 'starter-us-east-1',
          name: 'Starter: US East (Virginia)',
          type: 'starter',
          consoleUrl: 'https://console.starter-us-east-1.openshift.com'
        }
      },
    ]);
    mock.onGet(backendUrl + '/error').networkError();
    mock.onPost(backendUrl + '/cluster').reply(200, {result: 'updated'});
  });

  it('should perform http get', async done => {
    const response = await httpService.get(backendUrl, '/text');
    expect(response).toBe('response');
    done();
  });

  it('should perform http get for type', async done => {
    const response = await httpService.get<OpenShiftCluster[]>(backendUrl, '/cluster');
    expect(response[0].connected).toBe(false);
    done();
  });

  it('should preform http post', async done => {
    const payload = {name: 'the cluster'};
    const response = await httpService.post<{ name: string }, { result: string }>(backendUrl, '/cluster', payload);
    expect(response.result).toBe('updated');
    done();
  });

  it('should throw on error', async done => {
    try {
      await httpService.get(backendUrl, '/error');
    } catch (error) {
      expect(error.message).toEqual('An error occurred: Network Error');
      done();
    }
  });
});
