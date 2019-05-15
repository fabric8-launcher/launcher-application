import * as _ from 'lodash';
import { AnyExample, Catalog, Enums, Example, ExampleMission, ExampleRuntime, FieldProperty } from '../types';

export function fillPropsValuesWithEnums(propsContainer: { props?: FieldProperty[]; }, enums: Enums) {
  if (!propsContainer.props || propsContainer.props.length === 0) {
    return propsContainer;
  }
  const props = propsContainer.props.map(p => {
    return {
      ...fillPropsValuesWithEnums(p, enums),
      valuesWithEnums: enums[p.id],
    };
  });
  return {
    ...propsContainer,
    props,
  };
}

export function propsWithValuesMapper(enums: Enums) {
  return (c: { props?: FieldProperty[]; }) => fillPropsValuesWithEnums(c, enums);
}

const relations = ['example', 'mission', 'runtime', 'version'];

function copyProperties(obj: any, props: any, lambda?): any {
  const result = {};
  for (const prop in props) {
    if (props.hasOwnProperty(prop)) {
      const value = _.get(obj, prop);
      if (relations.indexOf(prop) === -1 || !lambda) {
        result[prop] = value;
      } else {
        result[prop] = lambda(value, obj, prop);
      }
    }
  }
  return result;
}

export function constructModel(catalog: Catalog): ExampleMission[] {
  const runtimeById = _.keyBy(catalog.runtimes, 'id');
  const versionsForRuntimeMission = _.groupBy(_.map(catalog.boosters, b => {
    const version = runtimeById[b.runtime as string].versions.find(v => v.id === b.version);
    version.metadata = version.metadata || {};
    version.metadata.runsOn = b.metadata ? (b.metadata.app.launcher ? b.metadata.app.launcher.runsOn : []) : [];
    return {
      key: (b.mission + '_' + b.runtime),
      version
    };
  }), 'key');

  const versionById = _.reduce(versionsForRuntimeMission, (result, versions) => {
    // @ts-ignore
    versions.map(version =>
      (result[version.key] || (result[version.key] = [])).push(version.version));
    return result;
  }, {});

  const runtimeForMission = _.reduce(_.map(catalog.boosters, b => ({
    key: b.mission,
    runtime: _.cloneDeep(runtimeById[b.runtime as string])
  })), (result, mission) => {
    // @ts-ignore
    const index = (result[mission.key] || (result[mission.key] = []));
    if (!index.find(r => r.id === mission.runtime.id)) {
      mission.runtime.versions = versionById[mission.key + '_' + mission.runtime.id];
      index.push(mission.runtime);
    }
    return result;
  }, {});

  return catalog.missions.map(m => {
    m.runtime = runtimeForMission[m.id];
    return m;
  });
}

export function filterExample(query: any, catalog: Catalog): Example[] {
  const missionById = _.keyBy(catalog.missions, 'id');
  const runtimeById = _.keyBy(catalog.runtimes, 'id');

  const result: Example[] = [];
  for (let i = 0; i < catalog.boosters.length; i++) {
    const example = catalog.boosters[i];
    result[i] = copyProperties(example, query,
      (name, obj, relation) =>
        copyProperties(relation === 'mission' ? missionById[name] : runtimeById[name], query[relation]));
  }
  return result;
}

export function filterExampleMission(query: any, catalog: Catalog): ExampleMission[] {
  const runtimeById = _.keyBy(catalog.runtimes, 'id');
  const lambda = (name, obj) => {
    if (query.runtime && query.runtime.id) {
      const runtime = catalog.boosters.filter(b => b.mission === obj.id).find(b => b.runtime === query.runtime.id);
      return runtime ? filterExampleRuntime(query.runtime, catalog) : null;
    }
    return _.uniqBy(catalog.boosters.filter(b => b.mission === obj.id), b => b.runtime).map(
      b => copyProperties(runtimeById[b.runtime as string], query.runtime));
  };

  const result: ExampleMission[] = [];
  if (query.id) {
    return [copyProperties(catalog.missions.filter(m => m.id === query.id)[0], query, lambda)];
  } else {
    for (let i = 0; i < catalog.missions.length; i++) {
      const mission = catalog.missions[i];
      result[i] = copyProperties(mission, query, lambda);
    }
  }

  return result;
}

export function filterExampleRuntime(query: any, catalog: Catalog): ExampleRuntime[] {
  const runtimeById = _.keyBy(catalog.runtimes, 'id');
  const lambda = (name, obj) => runtimeById[obj.id].versions.map(v => copyProperties(v, query.version));

  const result: ExampleRuntime[] = [];
  if (query.id) {
    return [copyProperties(runtimeById[query.id], query, lambda)];
  }
  for (let i = 0; i < catalog.runtimes.length; i++) {
    const runtime = catalog.runtimes[i];
    result[i] = copyProperties(runtime, query, lambda);
  }
  return result;
}

export function filter(query: any, catalog: Catalog): AnyExample[] {
  if (query.example) {
    return filterExample(query.example, catalog);
  }

  if (query.mission) {
    return filterExampleMission(query.mission, catalog);
  }

  if (query.runtime) {
    return filterExampleRuntime(query.runtime, catalog);
  }

  return [];
}

export function filterExamples(examples: Example[], cluster?: string, missionId?: string, runtimeId?: string, versionId?: string) {
  const availableExamples = examples.filter(b => {
    return (!missionId || b.mission === missionId)
      && (!runtimeId || b.runtime === runtimeId)
      && (!versionId || b.version === versionId);
  });
  if (availableExamples.length === 0) {
    return [];
  }
  if (!cluster) {
    return availableExamples;
  }
  const examplesRunningOnCluster = availableExamples.filter(b => {
    return checkRunsOnCluster(b, cluster);
  });
  if (examplesRunningOnCluster.length === 0) {
    return [];
  }
  return examplesRunningOnCluster;
}

function checkRunsOnCluster(example: Example, cluster: string) {
  let defaultResult = true;
  let runsOn = _.get(example, 'metadata.app.launcher.runsOn');
  if (typeof runsOn === 'string') {
    runsOn = [runsOn];
  }
  if (runsOn && runsOn.length !== 0) {
    for (const supportedCategory of runsOn) {
      if (!supportedCategory.startsWith('!')) {
        defaultResult = false;
      }
      if (supportedCategory.toLowerCase() === 'all'
        || supportedCategory.toLowerCase() === '*'
        || supportedCategory.toLocaleLowerCase() === cluster) {
        return true;
      } else if (supportedCategory.toLowerCase() === 'none'
        || supportedCategory.toLowerCase() === '!*'
        || supportedCategory.toLowerCase() === ('!' + cluster)) {
        return false;
      }
    }
  }
  return defaultResult;
}
