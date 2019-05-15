import { checkNotNull } from './preconditions';

export class Locations {

  public static joinPath(...parts: string[]): string {
    if (!parts || parts.length === 0) {
      return '';
    }
    let result = parts[0];
    for (const part of parts.slice(1)) {
      result = Locations.joinWithSlash(result, part);
    }
    return result;
  }

  public static joinWithSlash(start: string, end?: string): string {
    if (!end || !end.length) {
      return start;
    }
    let slashes = 0;
    if (start.endsWith('/')) {
      slashes++;
    }
    if (end.startsWith('/')) {
      slashes++;
    }
    if (slashes === 2) {
      return start + end.substring(1);
    }
    if (slashes === 1) {
      return start + end;
    }
    return `${start}/${end}`;
  }

  public static createWebsocketUrl(url: string) {
    checkNotNull(url, 'url');

    url = url!.substring(0, url.indexOf('/api'));
    if (url.indexOf('https') !== -1) {
      return url.replace('https', 'wss');
    } else if (url.indexOf('http') !== -1) {
      return url.replace('http', 'ws');
    } else if (url.startsWith('/') || url.startsWith(':')) {
      url = (url.startsWith(':') ? location.hostname : location.host) + url;
      return (location.protocol === 'https:' ? 'wss://' : 'ws://') + url;
    }
    throw new Error('Error while creating websocket url from url: ' + url);
  }
}
