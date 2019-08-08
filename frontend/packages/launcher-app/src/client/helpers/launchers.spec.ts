import exampleCatalog from '../data-examples/mock-example-catalog.json';
import capabilities from '../data-examples/mock-capabilities.json';
import enums from '../data-examples/mock-enums.json';

import { Capability, Catalog, Enums, Example, ExampleMission, ExampleRuntime } from '../types';
import { filter, filterExamples, propsWithValuesMapper } from './launchers';

describe('Filter examples', () => {
  it('should filter catalog on examples', () => {
    const catalog = exampleCatalog as unknown as Catalog;
    const result = filter({ example: { mission: { name }, runtime: { name }, name } }, catalog) as Example[];

    expect(result.length).toBeDefined();
    expect(result[0].name).toBe('Istio - Spring Boot - Security');
    expect(result[0].description).toBeUndefined();
    expect((result[0].mission as ExampleMission).name).toBe('Istio - Security');
    expect((result[0].runtime as ExampleRuntime).name).toBe('Spring Boot');
  });

  it('should filter catalog on missions', () => {
    const catalog = exampleCatalog as unknown as Catalog;
    const result = filter({ mission: { name: '', runtime: { id: '', icon: '' } } }, catalog) as ExampleMission[];

    expect(result.length).toBeDefined();
    expect(result[0].name).toBe('CRUD');
    expect(result[0].runtime!.length).toBe(5);
    expect(result[0].runtime![0].description).toBeUndefined();
    expect(result[0].runtime![0].icon).toBeDefined();
  });

  it('should filter catalog on mission id', () => {
    const result = filter({ mission: { id: 'rest-http-secured', name: '' } }, exampleCatalog as unknown as Catalog);

    expect(result.length).toBe(1);
    expect(result[0].name).toBe('Secured');
    expect((result[0] as ExampleMission).runtime).toBeUndefined();
  });

  it('should filter catalog on mission by missionId and runtimeId', () => {
    const result = filter({ mission: { id: 'crud', name: '', runtime: { id: 'vert.x', icon: '' } } },
      exampleCatalog as unknown as Catalog) as ExampleMission[];

    expect(result.length).toBeDefined();
    expect(result.length).toBe(1);
    expect(result[0].name).toBe('CRUD');
    expect(result[0].runtime!.length).toBe(1);
    expect(result[0].runtime![0].description).toBeUndefined();
  });

  it('should filter catalog on mission and runtime id', () => {
    const result = (filter({ mission: { id: '', name: '', runtime: { id: 'golang', icon: '' } } },
      exampleCatalog as unknown as Catalog) as ExampleMission[]).filter(m => !!m.runtime);

    expect(result.length).toBeDefined();
    expect(result.length).toBe(3);
    expect((result[0].runtime!.length)).toBe(1);
    expect((result[0].runtime![0]).description).toBeUndefined();
  });

  it('should filter catalog on runtime', () => {
    const result = filter({ runtime: { name, version: { name } } }, exampleCatalog as unknown as Catalog);

    expect(result.length).toBeDefined();
    expect(result[0].name).toBe('Eclipse Vert.x');
    const versions = (result[0] as any).version;
    expect(versions).toBeDefined();
    expect(versions.length).toBe(2);
    expect(versions[0].name).toBe('3.6.3 (Community)');
    expect(versions[0].id).toBeUndefined();
  });

  it('should filter catalog on runtime by id', () => {
    const result = filter({ runtime: { id: 'spring-boot', name } }, exampleCatalog as unknown as Catalog);

    expect(result.length).toBe(1);
    expect(result[0].name).toBe('Spring Boot');
  });

  it('should filter check runs on cluster', () => {
    const catalog = exampleCatalog as unknown as Catalog;
    let result = filterExamples(catalog.boosters, 'starter');

    expect(result).toBeDefined();
    expect(result.length).toBe(45);

    result = filterExamples(catalog.boosters, 'pro');

    expect(result).toBeDefined();
    expect(result.length).toBe(92);
  });

  it('should map props with values correctly', () => {
    const result = (capabilities as unknown as Capability[]).map(propsWithValuesMapper(enums as Enums));
    expect(result).toMatchSnapshot();
  });

});
