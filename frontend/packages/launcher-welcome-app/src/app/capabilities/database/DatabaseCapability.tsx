import { Grid, GridItem, TextInput } from '@patternfly/react-core';
import * as React from 'react';
import { HttpRequest, onRequestResult, RequestsConsole, useRequestsState } from '../../../shared/components/HttpRequest';
import { RequestTitle } from '../../../shared/components/RequestTitle';
import { SourceMappingLink } from '../../../shared/components/SourceMappingLink';
import { defaultIfEmpty } from '../../../shared/utils/Strings';
import CapabilityCard from '../../components/CapabilityCard';
import capabilitiesConfig from '../../config/capabilitiesConfig';
import { DATABASE_FRUIT_PATH, mockDatabaseCapabilityApi } from './DatabaseCapabilityApi';

export interface DatabaseCapabilityProps {
  databaseType?: string;
  sourceRepository?: {
    url: string;
    provider: string;
  };
  sourceMapping?: {
    dbEndpoint: string;
  };
}

export const DatabaseCapabilityApiContext = React.createContext(mockDatabaseCapabilityApi);

function GetFruits(props: { addRequestEntry: onRequestResult }) {
  const api = React.useContext(DatabaseCapabilityApiContext);
  const execFetchFruits = () => api.doFetchFruits();
  return (
    <HttpRequest
      method="GET"
      name="GET Fruits"
      path={DATABASE_FRUIT_PATH}
      url={api.getFruitsAbsoluteUrl()}
      execute={execFetchFruits}
      onRequestResult={props.addRequestEntry}
    />
  );
}

function PostFruit(props: { addRequestEntry: onRequestResult }) {
  const api = React.useContext(DatabaseCapabilityApiContext);
  const [name, setName] = React.useState<string>('');
  const [stock, setStock] = React.useState<string>('');

  const fruitData = {
    name: defaultIfEmpty(name, 'Coco'),
    stock: Number(defaultIfEmpty(stock, '10')),
  };

  const execPostFruit = () => api.doPostFruit(fruitData);

  return (
    <HttpRequest
      name="POST Fruit"
      method="POST"
      url={api.getFruitsAbsoluteUrl()}
      path={DATABASE_FRUIT_PATH}
      data={fruitData}
      execute={execPostFruit}
      onRequestResult={props.addRequestEntry}
    >
      <span style={{ marginLeft: '98px' }}>
        Name:
        <TextInput
          id="http-api-param-post-name-input"
          value={name}
          onChange={v => setName(v)}
          name="postName"
          placeholder="Coco"
          aria-label="Fruit to create"
          className="http-request-param"
        />
      </span>
      <span style={{ marginLeft: '50px' }}>
        Stock:
        <TextInput
          id="http-api-param-post-stock-input"
          value={stock}
          onChange={v => setStock(v)}
          name="postStock"
          placeholder="10"
          aria-label="Stock to create"
          className="http-request-param"
        />
      </span>
    </HttpRequest>
  );
}

function PutFruit(props: { addRequestEntry: onRequestResult }) {
  const api = React.useContext(DatabaseCapabilityApiContext);
  const [id, setId] = React.useState<string>('');
  const [name, setName] = React.useState<string>('');
  const [stock, setStock] = React.useState<string>('');

  const fruitId = Number(defaultIfEmpty(id, '2'));
  const fruitData = {
    name: defaultIfEmpty(name, 'Banana'),
    stock: Number(defaultIfEmpty(stock, '10')),
  };

  const execPutFruit = () => api.doPutFruit(fruitId, fruitData);

  return (
    <HttpRequest
      name="PUT Fruit"
      method="PUT"
      path={`${DATABASE_FRUIT_PATH}/`}
      url={`${api.getFruitsAbsoluteUrl()}/${fruitId}`}
      data={fruitData}
      execute={execPutFruit}
      onRequestResult={props.addRequestEntry}
    >
      <TextInput
        id="http-api-param-put-id-input"
        value={id}
        onChange={v => setId(v)}
        name="putId"
        placeholder="2"
        className="http-request-param"
        aria-label="Fruit ID to update"
        style={{ width: '40px' }}
      />
      <span style={{ marginLeft: '50px' }}>
        Name:
        <TextInput
          id="http-api-param-put-name-input"
          value={name}
          onChange={v => setName(v)}
          name="putName"
          placeholder="Banana"
          aria-label="New fruit name"
          className="http-request-param"
        />
      </span>
      <span style={{ marginLeft: '50px' }}>
        Stock:
        <TextInput
          id="http-api-param-put-stock-input"
          value={stock}
          onChange={v => setStock(v)}
          name="putStock"
          placeholder="10"
          aria-label="New fruit stock"
          className="http-request-param"
        />
      </span>
    </HttpRequest>
  );
}

function DeleteFruit(props: { addRequestEntry: onRequestResult }) {
  const api = React.useContext(DatabaseCapabilityApiContext);
  const [id, setId] = React.useState<string>('');

  const fruitId = Number(defaultIfEmpty(id, '2'));
  const execDeleteFruit = () => api.doDeleteFruit(fruitId);

  return (
    <HttpRequest
      name="DELETE Fruit"
      method="DELETE"
      path={`${DATABASE_FRUIT_PATH}/`}
      url={`${api.getFruitsAbsoluteUrl()}/${fruitId}`}
      execute={execDeleteFruit}
      onRequestResult={props.addRequestEntry}
    >
      <TextInput
        id="http-api-param-delete-id-input"
        value={id}
        onChange={v => setId(v)}
        name="deleteId"
        placeholder="2"
        className="http-request-param"
        aria-label="Fruit ID to delete"
        style={{ width: '40px' }}
      />
    </HttpRequest>
  );
}

export function DatabaseCapability(props: DatabaseCapabilityProps) {
  const [requests, addRequestEntry] = useRequestsState();
  return (
    <CapabilityCard module="database">
      <CapabilityCard.Title>{capabilitiesConfig.database.icon} {capabilitiesConfig.database.name}</CapabilityCard.Title>
      <CapabilityCard.Body>
        <Grid>
          <GridItem span={12}>
            As a starting point for your development,
            we have created a table and populated it with some data.
            We've additionally exposed CRUD operations via the following endpoints to give you a system that works end to end.
            </GridItem>
          <CapabilityCard.Separator />
          <GridItem span={12} className="http-request-service">
            <RequestTitle>
              <SourceMappingLink
                sourceRepository={props.sourceRepository}
                name="dbEndpoint"
                fileRepositoryLocation={props.sourceMapping && props.sourceMapping.dbEndpoint}
              />
            </RequestTitle>
          </GridItem>
          <GetFruits addRequestEntry={addRequestEntry} />
          <PostFruit addRequestEntry={addRequestEntry} />
          <PutFruit addRequestEntry={addRequestEntry} />
          <DeleteFruit addRequestEntry={addRequestEntry} />
          <GridItem span={12}>
            <RequestsConsole name="Database" requests={requests} />
          </GridItem>
          <CapabilityCard.Separator />
        </Grid>
      </CapabilityCard.Body>
    </CapabilityCard>
  );
}
