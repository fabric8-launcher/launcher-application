import * as React from 'react';
import { CodeBaseInfo } from '../CodeBaseInfo';
import { render } from 'react-testing-library';

const runtime = {
  name: 'Vert.x',
  description: `A
  tool-kit for building reactive applications on the
  JVM.`,
  icon: 'icon',
  metadata: {
    language: 'java',
  },
};

const sourceRepository = {
  url: 'https://github.com/fabric8-launcher/launcher-creator-welcome-app.git',
  provider: 'GitHub',
};

describe('<CodeBaseInfo />', () => {
  it('check that render is correct', () => {
    const component = render(<CodeBaseInfo sourceRepository={sourceRepository} />);
    expect(component.asFragment()).toMatchSnapshot();
  });

});
