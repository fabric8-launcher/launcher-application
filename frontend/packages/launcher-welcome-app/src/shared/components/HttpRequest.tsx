import { Button } from '@patternfly/react-core';
import * as React from 'react';
import { ReactNode } from 'react';
import { Console } from './Console';
import moment from 'moment-timezone';
// @ts-ignore
import JSONPretty from 'react-json-pretty';
import 'react-json-pretty/themes/monikai.css';
import './HttpRequest.scss';
import ShellCommand from './ShellCommand';

export type RequestMethod = 'GET' | 'POST' | 'DELETE' | 'PUT';

export interface RequestResult { content?: any; time: number; error?: string; }

export type onRequestResult = (method: RequestMethod, url: string, result: RequestResult) => void;

export interface RequestEntry {
  time: number;
  url: string;
  method: RequestMethod;
  content?: any;
  error?: string;
}

export function useRequestsState(): [RequestEntry[], onRequestResult] {
  const [requests, setRequests] = React.useState<RequestEntry[]>([]);
  const addRequestEntry = (type: RequestMethod, url: string, result: RequestResult) => {
    setRequests((prev) => [...prev, {
      method: type,
      ...result,
      url,
    }]);
  };
  return [requests, addRequestEntry];
}

interface HttpRequestProps {
  readonly method: RequestMethod;
  readonly name?: string;
  readonly path: string;
  readonly url: string;
  readonly data?: object;
  readonly children?: ReactNode;
  onRequestResult: onRequestResult;
  execute(): Promise<any>;
}

export function HttpRequest({ method, name, path, url, data, children, execute, onRequestResult }: HttpRequestProps) {
  const title = `Execute ${(name || 'the request')}`;
  const safeExecute = () => {
    execute()
      .then(r => onRequestResult(method, url, r))
      .catch(e => onRequestResult(method, url, {
        time: Date.now(),
        error: `An error occured while executing the request '${name}'`
      }));
  };
  let curlCommand = `curl -X ${method} '${url}'`
  if ((method === 'POST' || method === 'PUT') && data) {
    curlCommand += `--header 'Content-Type: application/json' `
      + `--data '${JSON.stringify(data)}'`;
  }
  return (
    <div className={`http-request method-${method.toLowerCase()}`}>
      <div className="definition">
        <div className="http-request-def">
          <span className="http-request-method">{method}</span> <span className="http-request-path">{path}</span>
          {children}
          {curlCommand && (<ShellCommand onlyButton={true} buttonText="Copy as curl" command={curlCommand} />)}
        </div>
      </div>
      <div className="action">
        <Button
          className={'http-request-button'}
          onClick={safeExecute}
          title={title}
          aria-label={title}
        >
          Execute
        </Button>
      </div>
    </div>
  );
};

export function RequestsConsole(props: { name: string, requests: RequestEntry[] }) {
  const res = props.requests.map((r, i) => (
    <React.Fragment key={i}>
      <div>
        <span className="prefix">$</span>&nbsp;
        <span className="time">{moment(r.time).format('LTS')}</span>&nbsp;
        <span className={`method method-${r.method.toLowerCase()}`}>{r.method}</span>&nbsp;
        <span className="url">{r.url}</span>:
      </div>
      {r.error && (<div className="error">{r.error}</div>)}
      {!r.error && !!r.content && typeof r.content === 'string' && (<div aria-label={r.content}>{r.content}</div>)}
      {!r.error && !!r.content && typeof r.content !== 'string' && (<div aria-label={JSON.stringify(r.content)}><JSONPretty json={r.content!} /></div>)}
      {!r.error && !r.content && (<div aria-label="OK">OK</div>)}
    </React.Fragment>
  ));

  return (
    <Console name={props.name} content={res} />
  );
}

