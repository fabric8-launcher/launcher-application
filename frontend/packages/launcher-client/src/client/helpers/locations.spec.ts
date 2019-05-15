import { Locations } from './locations';

describe('Location tool test', () => {
  it('should join multiple paths', () => {
    const location = Locations.joinPath('http://bla', 'some', '/with/', '/some', 'with/out');
    expect(location).toBe('http://bla/some/with/some/with/out');
  });

  it('should work with empty', () => {
    const location = Locations.joinPath();
    expect(location).toBe('');
  });

  it('should join with slash for empty', () => {
    const location = Locations.joinWithSlash('');
    expect(location).toBe('');
  });

  it('should join with slash for empty string', () => {
    const location = Locations.joinWithSlash('', '');
    expect(location).toBe('');
  });

  it('should join with slash for strings', () => {
    const location = Locations.joinWithSlash('toto/', 'titi');
    expect(location).toBe('toto/titi');
  });

  it('should create websocket url from standard url', () => {
    const url = 'https://basic-url/api';
    expect(Locations.createWebsocketUrl(url)).toBe('wss://basic-url');
  });

  it('should create websocket url from openshift url', () => {
    const url = ':8080/api';
    expect(Locations.createWebsocketUrl(url)).toBe('ws://localhost:8080');
  });

  it('should create websocket url http url', () => {
    const url = 'http://basic-url/api';
    expect(Locations.createWebsocketUrl(url)).toBe('ws://basic-url');
  });
});
