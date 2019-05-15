Fabric8-Launcher Frontend
=========================

[![CircleCI](https://circleci.com/gh/fabric8-launcher/launcher-frontend.svg?style=svg)](https://circleci.com/gh/fabric8-launcher/launcher-frontend)
[![codecov](https://codecov.io/gh/fabric8-launcher/launcher-frontend/branch/master/graph/badge.svg)](https://codecov.io/gh/fabric8-launcher/launcher-frontend)
[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&identifier=72209295)](https://dependabot.com)

Fabric8 Launcher frontend based on React.js.

If this is the first time you are starting the UI, you need to run

```bash
$ yarn install
```

## Development

### Start Storybook dev server (launcher-component)
```bash
$ yarn comp:storybook
```

### Build the libraries
All at once:
```bash
$ yarn libs:build
```

Or:
```bash
$ yarn client:build
$ yarn kc:build
$ yarn comp:build
```


### Start Launcher Application dev server (against staging-api)
```bash
$ yarn app:start
```

### Start Launcher Application dev server (against production-api)
```bash
$ yarn app:start:production-api
```

### Start Launcher Application dev server (against local-api - launcher-backend in 8080 and launcher-creator-backend in 8081)
```bash
$ yarn app:start:local-api
```

### Start Launcher Application dev server against a mock api
```bash
$ yarn app:start:mock-api
```

### Publish storybook
```bash
$ yarn comp:storybook:publish
```

## Test

```bash
$ yarn test
```

## Build for production

```bash
$ yarn build
```

### Storybook
https://fabric8-launcher-component-storybook.surge.sh

## Patternfly Doc
http://patternfly-react.surge.sh/patternfly-4/
