import React from 'react';
import { Alert, Card, CardBody, CardFooter, CardHeader, Grid, GridItem, Text, TextVariants } from '@patternfly/react-core';
import { CreateNewAppFlow } from '../flows/create-new-app-flow';
import { DeployExampleAppFlow } from '../flows/deploy-example-app-flow';
import style from './launcher.module.scss';
import { ImportExistingFlow } from '../flows/import-existing-flow';
import { CatalogIcon, FileImportIcon, TopologyIcon } from '@patternfly/react-icons';
import { useSessionStorage } from 'react-use-sessionstorage';
import { ButtonLink } from '@launcher/component';

import { launchEnabled } from '../app/config';
import { creatorEnabled } from '../app/config';

enum Type {
  NEW = 'NEW', EXAMPLE = 'EXAMPLE', IMPORT = 'IMPORT'
}

export interface LinkRef {
  href?: string;
  onClick(e): void;
}

export interface LauncherMenuProps {
  createNewApp: LinkRef;
  createExampleApp: LinkRef;
  importExistingApp: LinkRef;
}

export function Sunset() {
  return (
    <Alert
      variant="warning"
      title="This service is no longer being maintained and will be shut down in the near future"
      style={{margin: '10px 0'}}
    />
  );
}

export function LauncherMenu({createNewApp, createExampleApp, importExistingApp}: LauncherMenuProps) {
  const md = creatorEnabled && launchEnabled ? 4 : (creatorEnabled || launchEnabled ? 6 : 12);
  return (
    <Grid gutter="md" className={style.menu}>
      <GridItem span={12}>
        <Sunset/>
        <Text component={TextVariants.h1} className={style.title}>Launcher</Text>
        <Text component={TextVariants.p} className={style.description}>
          Create/Import your application, built and deployed on OpenShift.
        </Text>
      </GridItem>
      {creatorEnabled && <GridItem md={md} sm={12}>
        <Card className={style.card}>
          <CardHeader className={style.flowHeader}><TopologyIcon/></CardHeader>
          <CardBody>You start your own new application
            by picking the capabilities you want (Http Api, Persistence, ...).
            We take care of setting everything's up to get you started.</CardBody>
          <CardFooter>
            <ButtonLink variant="primary" {...createNewApp}>Create a New Application</ButtonLink>
          </CardFooter>
        </Card>
      </GridItem>}
      <GridItem md={md} sm={12}>
        <Card className={style.card}>
          <CardHeader className={style.flowHeader}><CatalogIcon/></CardHeader>
          <CardBody>Choose from a variety of Red Hat certified examples to generate the
            foundation for a new application in the OpenShift ecosystem.</CardBody>
          <CardFooter>
            <ButtonLink variant="primary" {...createExampleApp}>Deploy an Example Application</ButtonLink>
          </CardFooter>
        </Card>
      </GridItem>
      {launchEnabled && <GridItem md={md} sm={12} className={style.box}>
      <div className={style.ribbon}><span>Beta</span></div>
        <Card className={style.card}>
          <CardHeader className={style.flowHeader}><FileImportIcon/></CardHeader>
          <CardBody>Import your own existing application in the OpenShift ecosystem.</CardBody>
          <CardFooter>
            <ButtonLink variant="primary" {...importExistingApp}>
              Import an Existing Application
            </ButtonLink>
          </CardFooter>
        </Card>
      </GridItem>}
    </Grid>
  );
}

export function StateLauncher() {
  const [type, setType, clear] = useSessionStorage('type', '');
  const createNewApp = () => setType(Type.NEW);
  const createExampleApp = () => setType(Type.EXAMPLE);
  const importExistingApp = () => setType(Type.IMPORT);
  const resetType = () => {
    setType('');
    clear();
  };
  return (
    <div id="launcher-component" className={style.launcher}>
      {!type && (
        <LauncherMenu
          createNewApp={{onClick: createNewApp}}
          createExampleApp={{onClick: createExampleApp}}
          importExistingApp={{onClick: importExistingApp}}
        />
      )}
      {type && type === Type.NEW && (
        <CreateNewAppFlow onCancel={resetType}/>
      )}
      {type && type === Type.EXAMPLE && (
        <DeployExampleAppFlow onCancel={resetType}/>
      )}
      {type && type === Type.IMPORT && (
        <ImportExistingFlow onCancel={resetType}/>
      )}
    </div>
  );
}
