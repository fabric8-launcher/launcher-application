import * as React from 'react';
import { Button, Text, TextContent, TextVariants } from '@patternfly/react-core';
import { ClusterIcon, CodeIcon, GiftIcon } from '@patternfly/react-icons';
import { ExternalLink } from './external-link';
import { FixedModal } from '../core/stuff';
import { Fragment } from 'react';

interface LaunchNextStepsProps {
  links?: { [x: string]: string | undefined };
  onClose: () => void;
}

export function LaunchNextSteps(props: LaunchNextStepsProps) {
  const links = props.links || {};
  const repositoryLink = links['GITHUB_CREATE'];
  const consoleLink = links['OPENSHIFT_CREATE'];
  const welcomeLink = links['welcome'];
  return (
    <FixedModal
      title="Your Application deployment has started"
      isOpen
      isLarge={false}
      onClose={props.onClose}
      aria-label="Your Application has been launched"
      actions={[
        <Button key="launch-new" variant="secondary" onClick={props.onClose} aria-label="Start a new Application">
          Start a new Application
        </Button>,
      ]}
    >
      <TextContent>
        <Text component={TextVariants.h3}>Follow your application delivery</Text>
        <Text component={TextVariants.p}>You can follow your application deployment in your OpenShift Console</Text>
        {consoleLink && (<ExternalLink aria-label="Console link" href={consoleLink}>
          <ClusterIcon/> OpenShift Console
        </ExternalLink>)}
        {
          welcomeLink &&
          <Fragment>
              <Text component={TextVariants.h3}>As soon as deployment is done, check out your new application capabilities</Text>
              <Text component={TextVariants.p}>
                  We prepared a set of examples to let you directly start playing with your new application.<br/>
                  Those examples are there to get you started,<br/>
                  soon it will be time for you to remove them and start developing your awesome application.</Text>
              <ExternalLink href={welcomeLink} aria-label="Welcome Application link">
                  <GiftIcon/> Check out your new Application
              </ExternalLink>
          </Fragment>
        }
        <Text component={TextVariants.h3}>Update your application using Continuous Delivery</Text>
        <Text component={TextVariants.p}>We set up your application codebase in the GitHub repository you requested</Text>
        <Text component={TextVariants.p}>Your application is automatically configured
          to build and deploy on OpenShift with new commits.</Text>
        {repositoryLink && (<ExternalLink href={repositoryLink} aria-label="Repository link">
          <CodeIcon/> Clone your new codebase
        </ExternalLink>)}
      </TextContent>
    </FixedModal>
  );
}
