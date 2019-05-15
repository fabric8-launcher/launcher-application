import { HttpApi } from '../../../shared/utils/HttpApi';

export interface Fruit {
  id: number;
  name: string;
  stock: number;
}

export interface DatabaseCapabilityApi {
  getFruitsAbsoluteUrl(): string;

  doFetchFruits(): Promise<{ time: number, content: Fruit[] }>;

  doPostFruit(data: { name: string; stock: number }): Promise<{ time: number, content: Fruit }>;

  doPutFruit(id: number, data: { name: string; stock: number }): Promise<{ time: number; content: Fruit }>;

  doDeleteFruit(id: number): Promise<{ time: number }>;
}

export const DATABASE_FRUIT_PATH = '/api/fruits';

class HttpDatabaseCapabilityApi implements DatabaseCapabilityApi {

  constructor(private readonly httpApi: HttpApi) {
  }

  public getFruitsAbsoluteUrl(): string {
    return this.httpApi.getAbsoluteUrl(DATABASE_FRUIT_PATH);
  }

  public async doFetchFruits(): Promise<{ time: number, content: Fruit[] }> {
    const r = await this.httpApi.get<Fruit[]>(DATABASE_FRUIT_PATH);
    return ({content: r, time: Date.now()});
  }

  public async doPostFruit(data: { name: string; stock: number }): Promise<{ time: number; content: Fruit }> {
    const r = await this.httpApi.post<Fruit>(DATABASE_FRUIT_PATH, data);
    return ({content: r, time: Date.now()});
  }

  public async doPutFruit(id: number, data: { name: string; stock: number }): Promise<{ time: number; content: Fruit }> {
    const r = await this.httpApi.put<Fruit>(`${DATABASE_FRUIT_PATH}/${id}`, data);
    return ({content: r, time: Date.now()});
  }

  public async doDeleteFruit(id: number): Promise<{ time: number }> {
    await this.httpApi.delete(`${DATABASE_FRUIT_PATH}/${id}`);
    return {time: Date.now()};
  }
}

export const MOCK_FRUITS: Fruit[] = [{
  id: 1,
  name: 'Apple',
  stock: 10
}, {
  id: 2,
  name: 'Orange',
  stock: 10
}, {
  id: 3,
  name: 'Pear',
  stock: 10
}];

class MockDatabaseCapabilityApi implements DatabaseCapabilityApi {

  private fruits: Fruit[] = MOCK_FRUITS;

  public getFruitsAbsoluteUrl(): string {
    return `http://mocked.io/api/fruits`;
  }

  public async doFetchFruits(): Promise<{ time: number, content: Fruit[] }> {
    return {
      time: Date.now(),
      content: this.fruits,
    };
  }

  public async doPostFruit(data: { name: string; stock: number }): Promise<{ time: number; content: Fruit }> {
    return {
      time: Date.now(),
      content: {...data, id: 4},
    };
  }

  public async doDeleteFruit(id: number): Promise<{ time: number }> {
    return {time: Date.now()};
  }

  public async doPutFruit(id: number, data: { name: string; stock: number }): Promise<{ time: number; content: Fruit }> {
    return {
      time: Date.now(),
      content: {...data, id},
    };
  }
}

export function newMockDatabaseCapabilityApi():DatabaseCapabilityApi { return new MockDatabaseCapabilityApi(); }

export const mockDatabaseCapabilityApi:DatabaseCapabilityApi = newMockDatabaseCapabilityApi();

export function newHttpDatabaseCapabilityApi(httpApi: HttpApi): DatabaseCapabilityApi {
  return new HttpDatabaseCapabilityApi(httpApi);
}
