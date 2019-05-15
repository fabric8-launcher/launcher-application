import axios from 'axios';
import MockAdaptor from 'axios-mock-adapter';
import {
  AUTH_HEADER_KEY,
  GIT_AUTH_HEADER_KEY,
  OpenshiftAuthenticationApi,
  OPENSHIFT_AUTH_HEADER_KEY,
  OPENSHIFT_AUTH_STORAGE_KEY,
} from './openshift-authentication-api';

describe('Openshift authentication', () => {
  const tokenUri = 'http://token_uri/';
  const authentication = new OpenshiftAuthenticationApi({
    openshift: { validateTokenUri: tokenUri },
    github: { validateTokenUri: '/launch/github/access_token' },
    gitProvider: 'github'
  } as any);
  const mock = new MockAdaptor(axios);

  beforeEach(() => {
    Object.defineProperty(global, '_localStorage', {
      value: new FakeLocalStorage(),
      writable: false,
    });
  });

  it('should be undefined', async () => {
    // when
    const user = await authentication.init();

    expect(user).toBeUndefined();
  });

  it('should create valid login url', async () => {
    const redirectTestAuth = new OpenshiftAuthenticationApi({ openshift: { url: 'http://auth', clientId: 'demo' }, github: {} } as any);
    Object.defineProperty(window.location, 'assign', {
      writable: true,
      value: jest.fn()
    });

    await redirectTestAuth.login();

    expect(window.location.assign).toBeCalledWith('http://auth?client_id=demo&response_type=token&redirect_uri=http%3A%2F%2Flocalhost%2F');
  });

  it('should get user when token on url', async () => {
    // given
    location.hash = '#access_token=1235';
    mock.onGet(tokenUri).reply(200, '{"name": "developer"}');

    // when
    const user = await authentication.init();

    expect(localStorage.getItem).toBeCalledTimes(1);
    expect(user).toBeDefined();
    expect(user!.userName).toBe('developer');
    expect(user!.authorizationsByProvider.openshift).toBeDefined();
    expect(user!.authorizationsByProvider.openshift![OPENSHIFT_AUTH_HEADER_KEY]).toBe('Bearer 1235');
  });

  it('should validate token of stored user', async () => {
    // given
    localStorage._STORE_[OPENSHIFT_AUTH_STORAGE_KEY] = JSON.stringify({
      authorizationsByProvider: {
        openshift: {
          [AUTH_HEADER_KEY]: 123,
          [OPENSHIFT_AUTH_HEADER_KEY]: `Bearer 123`,
        }
      }
    });
    mock.onGet(tokenUri).reply(200, 'ignored');

    // when
    const user = await authentication.init();

    expect(localStorage.getItem).toBeCalled();
    expect(user).toBeDefined();
    expect(user!.authorizationsByProvider.openshift![OPENSHIFT_AUTH_HEADER_KEY]).toBe('Bearer 123');
  });

  it('should logout on invalid token', async () => {
    // given
    localStorage._STORE_[OPENSHIFT_AUTH_STORAGE_KEY] = JSON.stringify({
      authorizationsByProvider: {
        openshift: {
          [AUTH_HEADER_KEY]: 123,
          [OPENSHIFT_AUTH_HEADER_KEY]: `Bearer 123`,
        }
      }
    });
    mock.onGet(tokenUri).reply(401);
    window.location.assign = jest.fn();

    // when
    await authentication.init();

    expect(localStorage.getItem).toBeCalled();
    expect(localStorage.removeItem).toBeCalled();
    expect(authentication.user).toBeUndefined();
  });

  it('should clean url correctly', () => {
    // @ts-ignore
    const cleanUrl =authentication.cleanUrl;
    expect(cleanUrl('http://www.url.fr')).toBe('http://www.url.fr');
    expect(cleanUrl('http://www.url.fr/?code=222hh32737f#totot')).toBe('http://www.url.fr/');
    expect(cleanUrl('http://www.url.fr/?code=222hh32737f&request=/flow/new-app#totot'))
      .toBe('http://www.url.fr/?request=/flow/new-app');
    expect(cleanUrl('http://www.url.fr/?request=/flow/new-app&code=2333j33#totot'))
      .toBe('http://www.url.fr/?request=/flow/new-app');
  });

  it('should fetch github access token', async () => {
    // given
    localStorage._STORE_[OPENSHIFT_AUTH_STORAGE_KEY] = JSON.stringify({
      authorizationsByProvider: {
        openshift: {
          [AUTH_HEADER_KEY]: 123,
          [OPENSHIFT_AUTH_HEADER_KEY]: `Bearer 123`,
        }
      }
    });
    location.hash = '?code=githubcode'; // mock query part of url
    mock.onGet(tokenUri).reply(200, '{"name": "developer"}');
    mock.onPost('/launch/github/access_token').reply(200, '{"access_token": "super"}');

    // when
    const user = await authentication.init();

    expect(user).toBeDefined();
    expect(user!.authorizationsByProvider.openshift).toBeDefined();
    expect(user!.authorizationsByProvider.openshift).toBeDefined();
    expect(user!.authorizationsByProvider.openshift![OPENSHIFT_AUTH_HEADER_KEY]).toBe('Bearer 123');
    expect(user!.authorizationsByProvider.git![GIT_AUTH_HEADER_KEY]).toBe('Bearer super');
  });

  it('should fetch gitea access token', async () => {
    // given
    localStorage._STORE_[OPENSHIFT_AUTH_STORAGE_KEY] = JSON.stringify({
      authorizationsByProvider: {
        openshift: {
          [AUTH_HEADER_KEY]: 123,
          [OPENSHIFT_AUTH_HEADER_KEY]: `Bearer 123`,
        }
      }
    });
    location.hash = '?code=giteacode'; // mock query part of url
    mock.onGet(tokenUri).reply(200, '{"name": "developer"}');
    mock.onPost('/launch/gitea/access_token').reply(200, '{"access_token": "gitea is also super"}');

    const authentication = new OpenshiftAuthenticationApi({
      openshift: { validateTokenUri: tokenUri },
      gitea: { validateTokenUri: '/launch/gitea/access_token' },
      gitProvider: 'gitea'
    } as any);

    // when
    const user = await authentication.init();

    expect(user).toBeDefined();
    expect(user!.authorizationsByProvider.openshift).toBeDefined();
    expect(user!.authorizationsByProvider.openshift).toBeDefined();
    expect(user!.authorizationsByProvider.openshift![OPENSHIFT_AUTH_HEADER_KEY]).toBe('Bearer 123');
    expect(user!.authorizationsByProvider.git![GIT_AUTH_HEADER_KEY]).toBe('Bearer gitea is also super');
  });

});

export class FakeLocalStorage {
  constructor() {
    Object.defineProperty(this, 'getItem', {
      enumerable: false,
      // @ts-ignore
      value: jest.fn(key => this[key] || null),
    });
    Object.defineProperty(this, 'setItem', {
      enumerable: false,
      value: jest.fn((key, val = '') => {
        // @ts-ignore
        this[key] = `${val}`;
      }),
    });
    Object.defineProperty(this, 'removeItem', {
      enumerable: false,
      value: jest.fn(key => {
        // @ts-ignore
        delete this[key];
      }),
    });
  }

  get length() {
    return Object.keys(this).length;
  }

  get _STORE_() {
    return this;
  }
}
