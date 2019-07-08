import * as React from 'react';
import { CloudDeploymentInfo } from '../CloudDeploymentInfo';
import { render } from '@testing-library/react';

describe('<CloudDeploymentInfo />', () => {
  it('check that render is correct', () => {
    const component = render(<CloudDeploymentInfo applicationName="App name" applicationUrl="http://www.application-url.com"/>);
    expect(component.asFragment()).toMatchSnapshot();
  });

  it('check that render is correct with repositoryUrl', () => {
    const component = render(
      <CloudDeploymentInfo
        applicationName="App name"
        applicationUrl="http://www.application-url.com"
        openshiftConsoleUrl="http://www.console-url.com"
      />
    );
    expect(component.asFragment()).toMatchSnapshot();
  });

});
