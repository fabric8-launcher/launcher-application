import React from 'react';
import { storiesOf } from '@storybook/react';
import { action } from '@storybook/addon-actions';

import { FormPanel } from '../../core/form-panel/form-panel';
import { OpenshiftClustersLoader } from '../../loaders/openshiftcluster-loader';
import { ClusterPicker } from '../cluster-picker';
import { LauncherDepsProvider } from '../..';

function authorizationLinkGenerator(id?: string) {
  return `http://www.authorize-cluster.com/${id || ''}`;
}

storiesOf('Pickers', module)
  .addDecorator((storyFn) => (
    <LauncherDepsProvider>
      {storyFn()}
    </LauncherDepsProvider>
  ))
  .add('ClusterPicker', () => {
    return (
      <OpenshiftClustersLoader>
        {clusters => (
          <FormPanel
            initialValue={{}}
            validator={ClusterPicker.checkCompletion}
            onSave={action('save')}
            onCancel={action('cancel')}
          >
            {(inputProps) => (
              <ClusterPicker.Element {...inputProps} clusters={clusters} authorizationLinkGenerator={authorizationLinkGenerator}/>)}
          </FormPanel>
        )}
      </OpenshiftClustersLoader>
    );
  })
  .add('ClusterPicker: Multiple choices', () => {
    return (
      <OpenshiftClustersLoader>
        {clusters => {
          clusters[1].connected = true;
          return (
            <FormPanel
              initialValue={{}}
              validator={ClusterPicker.checkCompletion}
              onSave={action('save')}
              onCancel={action('cancel')}
            >
              {(inputProps) => (
                <ClusterPicker.Element {...inputProps} clusters={clusters} authorizationLinkGenerator={authorizationLinkGenerator}/>)}
            </FormPanel>
          );
        }}
      </OpenshiftClustersLoader>
    );
  })
  .add('ClusterPicker: EmptyState', () => {
    return (
      <FormPanel
        initialValue={{}}
        validator={ClusterPicker.checkCompletion}
        onSave={action('save')}
        onCancel={action('cancel')}
      >
        {(inputProps) => (<ClusterPicker.Element {...inputProps} clusters={[]} authorizationLinkGenerator={authorizationLinkGenerator}/>)}
      </FormPanel>
    );
  });
