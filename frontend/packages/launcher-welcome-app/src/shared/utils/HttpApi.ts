import axios, { AxiosInstance } from 'axios';
import { getLocationAbsoluteUrl } from './Locations';

export interface HttpApi {
  readonly baseUrl: string;

  getAbsoluteUrl(path: string): string;

  get<T>(path: string): Promise<T>;

  put<T>(path: string, data?: any): Promise<T>;

  post<T>(path: string, data?: any): Promise<T>;

  delete(path: string): Promise<any>;
}

export class AxiosHttpApi implements HttpApi {

  public readonly baseUrl: string;
  private axios: AxiosInstance;

  constructor(config: { serviceUrl: string }) {
    this.axios = axios.create({
      baseURL: config.serviceUrl,
    });
    this.baseUrl = config.serviceUrl;
  }

  public get<T>(path: string): Promise<T> {
    return this.axios.get<T>(path).then(r => r.data);
  }

  public put<T>(path: string, data?: any): Promise<T> {
    return this.axios.put<T>(path, data).then(r => r.data);
  }

  public post<T>(path: string, data?: any): Promise<T> {
    return this.axios.post<T>(path, data).then(r => r.data);
  }

  public delete(path: string): Promise<any> {
    return this.axios.delete(path).then(r => r.data);
  }

  public getAbsoluteUrl(path: string) {
    return getLocationAbsoluteUrl(this.baseUrl + path);
  }

}
