import * as React from 'react';
import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Gallery,
  GalleryItem,
  PageSection,
  PageSectionVariants,
  Text,
  TextContent,
  TextVariants
} from '@patternfly/react-core';
import style from './login-page.module.scss';
import { Layout } from './layout';
import { NewAppRuntimesLoader } from 'launcher-component';
import { ExternalLinkSquareAltIcon } from '@patternfly/react-icons';
import { PropertyValue } from 'launcher-client';
import { useAuthenticationApi } from '../auth/auth-context';

function LoginCard() {
  const auth = useAuthenticationApi();
  return (
    <div className={style.loginCard}>
      <p className={style.loginText}>
        When you click on start, you will first have to login or register an account for free
        with the Red Hat Developer Program.
      </p>
      <Button variant="primary" onClick={auth.login} className={style.loginButton}>
        Start
      </Button>
    </div>
  );
}

type RuntimeProps = PropertyValue;

function Runtime(props: RuntimeProps) {
  return (
    <Card className={style.card}>
      <CardHeader className={style.header}><img src={props.icon} alt={props.name} /></CardHeader>
      <CardBody>{props.description}</CardBody>
      {props.metadata && props.metadata.website && (
        <CardFooter>
          <a href={props.metadata.website} target="_blank">
            Learn more <ExternalLinkSquareAltIcon />
          </a>
        </CardFooter>
      )}
    </Card>
  );
}

export const LoginPage = () => (
  <Layout>
    <section className={style.intro}>
      <div className="container">
        <h1 className={style.mainTitle}>Launcher</h1>
        <h2 className={style.subTitle}>Create/Import your application,</h2>
        <h2 className={style.subTitle}>built and deployed on OpenShift.</h2>
        <LoginCard />
      </div>
    </section>
    <PageSection variant={PageSectionVariants.light} style={{paddingBottom: 0}}>
      <TextContent style={{margin: 0}}>
        <Text component={TextVariants.h1}>Supported Backend Runtimes</Text>
      </TextContent>
    </PageSection>
    <PageSection variant={PageSectionVariants.light} className={style.container}>
      <NewAppRuntimesLoader category="backend">
        {runtimes => runtimes.map(r => (<Runtime {...r} key={r.id} />))}
      </NewAppRuntimesLoader>
    </PageSection>
    <PageSection variant={PageSectionVariants.light}>
      <TextContent>
        <Text component={TextVariants.h1}>Supported Frontend Frameworks</Text>
      </TextContent>
    </PageSection>
    <PageSection variant={PageSectionVariants.light} className={style.container}>
      <NewAppRuntimesLoader category="frontend">
        {runtimes => runtimes.map(r => (<Runtime {...r} key={r.id} />))}
      </NewAppRuntimesLoader>
    </PageSection>
  </Layout>
);
