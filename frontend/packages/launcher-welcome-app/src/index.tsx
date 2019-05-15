import * as React from 'react';
import * as ReactDOM from 'react-dom';
import '@patternfly/react-core/dist/styles/base.css';
import './index.scss';

import * as serviceWorker from './service-worker';
import App from './app/App';

ReactDOM.render(
  <App />,
  document.getElementById('root') as HTMLElement
);
serviceWorker.unregister();
