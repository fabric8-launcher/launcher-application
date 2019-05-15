import { Card, CardBody, CardFooter, CardHeader, Grid, GridItem, Text, TextVariants } from '@patternfly/react-core';
import * as React from 'react';
import { CreateNewAppFlow } from '../flows/create-new-app-flow';
import { DeployExampleAppFlow } from '../flows/deploy-example-app-flow';
import style from './launcher.module.scss';
import { ImportExistingFlow } from '../flows/import-existing-flow';
import { CatalogIcon, FileImportIcon, TopologyIcon } from '@patternfly/react-icons';
import { useSessionStorage } from 'react-use-sessionstorage';
import { ButtonLink } from '../core/stuff';

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

export function LauncherMenu({createNewApp, createExampleApp, importExistingApp}: LauncherMenuProps) {
  return (
    <Grid gutter="md" className={style.menu}>
      <GridItem span={12}>
        <Text component={TextVariants.h1} className={style.title}>Launcher</Text>
        <Text component={TextVariants.p} className={style.description}>
          Create/Import your application, built and deployed on OpenShift.
        </Text>
      </GridItem>
      <GridItem md={4} sm={12}>
        <Card className={style.card}>
          <CardHeader className={style.flowHeader}><TopologyIcon/></CardHeader>
          <CardBody>You start your own new application
            by picking the capabilities you want (Http Api, Persistence, ...).
            We take care of setting everything's up to get you started.</CardBody>
          <CardFooter>
            <ButtonLink variant="primary" {...createNewApp}>Create a New Application</ButtonLink>
          </CardFooter>
        </Card>
      </GridItem>
      <GridItem md={4} sm={12}>
        <Card className={style.card}>
          <CardHeader className={style.flowHeader}><CatalogIcon/></CardHeader>
          <CardBody>Choose from a variety of Red Hat certified examples to generate the
            foundation for a new application in the OpenShift ecosystem.</CardBody>
          <CardFooter>
            <ButtonLink variant="primary" {...createExampleApp}>Deploy an Example Application</ButtonLink>
          </CardFooter>
        </Card>
      </GridItem>
      <GridItem md={4} sm={12} className={style.box}>
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
      </GridItem>
    </Grid>
  );
}

export function StateLauncher() {
  const [type, setType, clear] = useSessionStorage('type', '');
  const createNewApp = (e) => setType(Type.NEW);
  const createExampleApp = (e) => setType(Type.EXAMPLE);
  const importExistingApp = (e) => setType(Type.IMPORT);
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
