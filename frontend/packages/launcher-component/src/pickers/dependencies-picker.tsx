import { Grid, GridItem, Stack, StackItem, TextInput, Title, Tooltip } from "@patternfly/react-core";
import { PlusIcon, TimesIcon } from "@patternfly/react-icons";
import React, { Fragment, useState } from "react";
import { InputProps, Picker } from "../core/types";
import style from './dependencies.module.scss';

export interface DependencyItem {
  id: string;
  name: string;
  description: string;
  metadata: { category: string; };
}

export interface DependenciesPickerValue {
  dependencies?: string[];
}

interface DependenciesPickerProps extends InputProps<DependenciesPickerValue> {
  items: DependencyItem[];
  placeholder: string;
}

enum OperationType {
  Add = 1,
  Remove,
}

interface DependencyItemProps extends DependencyItem {
  operation?: OperationType;
  onClick(id: string): void;
}

function DependencyItemComponent(props: DependencyItemProps) {
  const [active, setActive] = useState(false);
  const onClick = () => {
    props.onClick(props.id);
  };

  return (
    <div
      className={`${style.item} ${active ? style.active : ''}`}
      onMouseEnter={() => setActive(true)}
      onMouseLeave={() => setActive(false)}
      onClick={onClick}
    >
      <Stack style={{ position: 'relative' }}>
        <StackItem isMain>
          <Title size="sm" aria-label={`Pick ${props.id} dependency`}>{props.name}</Title>
          <span className={style.category}>{props.metadata.category}</span>
          {active && (props.operation === OperationType.Add ?
            <PlusIcon className={style.icon} /> : <TimesIcon className={style.icon} />)}
        </StackItem>
        <StackItem isMain={false}>{props.description}</StackItem>
      </Stack>
    </div>
  )
}

export const DependenciesPicker: Picker<DependenciesPickerProps, DependenciesPickerValue> = {
  checkCompletion: (value: DependenciesPickerValue) => !!value.dependencies && value.dependencies.length > 0,
  Element: (props: DependenciesPickerProps) => {
    const [filter, setFilter] = useState('');
    const dependencies = props.value.dependencies || [];
    const dependenciesSet = new Set(dependencies);
    const dependencyItemById = new Map(props.items.map(item => [item.id, item]));

    const addDep = (id: string) => {
      dependenciesSet.add(id);
      props.onChange({ dependencies: Array.from(dependenciesSet) });
    };

    const removeDep = (id: string) => {
      dependenciesSet.delete(id);
      props.onChange({ dependencies: Array.from(dependenciesSet) });
    };

    const filterFunction = (d: DependencyItem) =>
      filter !== '' && (d.description.toLowerCase().includes(filter.toLowerCase())
        || d.name.toLowerCase().includes(filter.toLowerCase())
        || d.metadata.category.toLowerCase().includes(filter.toLowerCase()));
    const result = props.items.filter(filterFunction);
    const categories = new Set(props.items.map(i => i.metadata.category));
    return (
      <Fragment>
        <Grid gutter="md">
          <GridItem sm={12} md={6}>
            <Tooltip position="right" content={`${Array.from(categories).join(', ')}`}>
              <TextInput
                aria-label="Search dependencies"
                placeholder={props.placeholder}
                value={filter}
                onChange={value => setFilter(value)}
              />
            </Tooltip>
            <div aria-label="Select dependencies" className={style.dependencyList}>
              {
                result.map((dep, i) => (
                  <DependencyItemComponent
                    operation={OperationType.Add}
                    {...dep}
                    key={i}
                    onClick={addDep}
                  />
                ))
              }
              {filter && !result.length && <Title size="xs" style={{ paddingTop: '10px' }}>No result.</Title>}
            </div>
          </GridItem>
          {dependencies.length > 0 && (
            <GridItem sm={12} md={6}>
              <Title size="md">Selected:</Title>
              <div className={style.dependencyList}>
                {
                  dependencies.map((selected, i) => (
                    <DependencyItemComponent
                      operation={OperationType.Remove}
                      {...dependencyItemById.get(selected)!}
                      key={i}
                      onClick={removeDep}
                    />
                  ))
                }
              </div>
            </GridItem>
          )}
        </Grid>
      </Fragment>
    );
  }
}