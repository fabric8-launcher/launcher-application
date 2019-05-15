import * as React from 'react';
import { Button, Text, TextContent, TextVariants } from '@patternfly/react-core';
import { DownloadIcon } from '@patternfly/react-icons';
import { ExternalLink } from './external-link';
import { FixedModal } from '../core/stuff';

interface DownloadNextStepsProps {
  onClose: () => void;
  downloadLink?: string;
}

export function DownloadNextSteps(props: DownloadNextStepsProps) {
  return (
    <FixedModal
      title="Your Application is Ready"
      isOpen
      isLarge={false}
      onClose={props.onClose}
      aria-label="Your Application is ready to be downloaded"
      actions={[
        <Button key="launch-new" variant="secondary" aria-label="Start a new Application" onClick={props.onClose}>
          Start a new Application
        </Button>,
      ]}
    >
      <TextContent>
        <Text component={TextVariants.h3}>Download your application</Text>
        <Text component={TextVariants.p}>
          You are ready to start working.
        </Text>
        <ExternalLink href={props.downloadLink as string} aria-label="Download link">
          <DownloadIcon/> Download .zip
        </ExternalLink>
        <Text component={TextVariants.h3}>Deploy it on OpenShift</Text>
        <Text component={TextVariants.p}>
          Your new application contains a tool to help you deploy your new application on OpenShift.<br/>
          You can find instructions in the README.md.
        </Text>
        <Text component={TextVariants.h3}>As soon as deployment is done, go checkout your new application capabilities</Text>
        <Text component={TextVariants.p}>We prepared a set of examples to let you directly start playing with your new application.<br/>
          Those examples are there to get you started,<br/>
          soon it will be time for you to remove them and start developing your awesome application.</Text>
      </TextContent>
    </FixedModal>
  );
}
