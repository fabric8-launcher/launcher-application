import React from 'react';
import '@patternfly/react-core/dist/styles/base.css';
import { storiesOf } from '@storybook/react';
import { TogglePanel } from '../toggle-panel';

storiesOf('Core', module)
  .add('TogglePanel', () => (
    <TogglePanel title="Click here to expand!">
      <div>
        <h1>Title</h1>
        <p>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
          Auctor elit sed vulputate mi sit amet mauris. Et netus et malesuada fames ac turpis egestas. Lorem ipsum dolor sit amet
          consectetur adipiscing elit duis. Id aliquet lectus proin nibh nisl. Felis bibendum ut tristique et. Interdum varius sit
          amet mattis vulputate enim nulla. Dignissim enim sit amet venenatis urna cursus eget nunc. Magna ac placerat vestibulum
          lectus mauris ultrices eros. Elit ut aliquam purus sit amet luctus venenatis lectus magna.
        </p>
      </div>
    </TogglePanel>
  ));
