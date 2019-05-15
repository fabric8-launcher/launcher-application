import { HttpApi } from '../../../shared/utils/HttpApi';

export interface RestCapabilityApi {
  getGreetingAbsoluteUrl(name: string): string;

  doGetGreeting(name: string): Promise<{ content: string, time: number }>;
}

function buildGreetingPath(path: string, name: string) {
  return name ? `${path}?name=${encodeURIComponent(name)}` : path;
}

export const REST_GREETING_PATH = '/api/greeting';

class HttpRestCapabilityApi implements RestCapabilityApi {

  constructor(private readonly httpApi: HttpApi) {
  }

  public async doGetGreeting(name: string): Promise<{ content: string, time: number }> {
    const greetingPath = buildGreetingPath(REST_GREETING_PATH, name);
    const r = await this.httpApi.get<{ content: string; }>(greetingPath);
    return ({content: r.content, time: Date.now()});
  }

  public getGreetingAbsoluteUrl(name: string): string {
    return this.httpApi.getAbsoluteUrl(buildGreetingPath(REST_GREETING_PATH, name));
  }
}

class MockRestCapabilityApi implements RestCapabilityApi {
  public async doGetGreeting(name: string): Promise<{ content: string, time: number }> {
    return {content: `Hello ${(name || 'World')}!`, time: Date.now()};
  }

  public getGreetingAbsoluteUrl(name: string): string {
    return `http://mocked.io${buildGreetingPath(REST_GREETING_PATH, name)}`;
  }
}

export function newMockRestCapabilityApi(): RestCapabilityApi { return new MockRestCapabilityApi(); }

export const mockRestCapabilityApi = newMockRestCapabilityApi();

export function newHttpRestCapabilityApi(httpApi: HttpApi): RestCapabilityApi {
  return new HttpRestCapabilityApi(httpApi);
}
