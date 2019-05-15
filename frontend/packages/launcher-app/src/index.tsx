import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import { LauncherApp } from './app/launcher-app';
import * as serviceWorker from './service-worker';
import { SentryBoundary } from './sentry-boundary';

ReactDOM.render(<SentryBoundary><LauncherApp /></SentryBoundary>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();
