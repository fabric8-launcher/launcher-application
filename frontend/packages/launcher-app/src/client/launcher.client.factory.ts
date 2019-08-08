import { HttpService } from './http.service';
import DefaultLauncherClient from './impl/default.launcher.client';
import { LauncherClientConfig } from './types';
/* test-code */
import MockLauncherClient from './impl/mock.launcher.client';
/* end-test-code */
import { ReflectiveInjector } from 'injection-js';
import WithCacheLauncherClient from './impl/with-cache.launcher.client';

export function defaultLauncherClient(config: LauncherClientConfig) {
  const injector = ReflectiveInjector.resolveAndCreate([HttpService]);
  return new DefaultLauncherClient(injector.get(HttpService), config);
}

export function cachedLauncherClient(config: LauncherClientConfig) {
  const injector = ReflectiveInjector.resolveAndCreate([HttpService]);
  return new WithCacheLauncherClient(new DefaultLauncherClient(injector.get(HttpService), config));
}

/* test-code */
export function mockLauncherClient() {
  return new MockLauncherClient();
}
/* end-test-code */
