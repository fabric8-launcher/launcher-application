import { Capability, propsWithValuesMapper, checkNotNull } from '@launcher/client';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { DataLoader } from '../core/data-loader/data-loader';
import { CapabilityItem } from '../pickers/capabilities-picker';
import * as React from 'react';

export function capabilityToItem(c: Capability): CapabilityItem {
  return {
    id: c.module,
    name: c.name,
    description: c.description,
    category: c.metadata.category,
    icon: c.metadata.icon,
    fields: c.props,
    disabled: c.module === 'health'
  };
}

export function capabilityMatcherByCategories(...categories: string[]) {
  return (c: Capability) => categories.indexOf(c.metadata.category) >= 0;
}

export function getCapabilityRuntimeNameProp(c: Capability) {
  const runtimeProp = checkNotNull(c.props.find(p => p.id === 'runtime'), 'runtime prop');
  const runtimeProps = checkNotNull(runtimeProp.props, 'runtimeProps');
  return checkNotNull(runtimeProps.find(p => p.id === 'name'), 'runtime name prop');
}

function capabilityMatcherForRuntime(runtime?: string) {
  return (c: Capability) => {
    if (!runtime) {
      return true;
    }
    const runtimeNameProp = getCapabilityRuntimeNameProp(c);
    return runtimeNameProp.values && runtimeNameProp.values.indexOf(runtime) >= 0;
  };
}

export const readOnlyCapabilities = [{ id: 'health', selected: true }];

export function NewAppCapabilitiesLoader(props: { categories: string[], runtime?: string, children: (capabilities: Capability[]) => any }) {
  const client = useLauncherClient();
  const itemsLoader = async () => {
    const [c, e] = await Promise.all([client.capabilities(), client.enums()]);
    return c.filter(capabilityMatcherByCategories(...props.categories))
      .map(propsWithValuesMapper(e))
      .filter(capabilityMatcherForRuntime(props.runtime));
  };
  return (
    <DataLoader loader={itemsLoader} deps={[props.runtime, props.categories]}>
      {props.children}
    </DataLoader>
  );
}

export function NewAppCapabilitiesByModuleLoader(props: { categories: string[], children: (capabilitiesById: Map<string, Capability>) => any }) {
  return (
    <NewAppCapabilitiesLoader categories={props.categories} >
      {(capabilities) => props.children(new Map(capabilities.map(c => [c.module, c] as [string, Capability])))}
    </NewAppCapabilitiesLoader>
  );
}
