import React from 'react';
import { render } from 'react-testing-library';

import { InlineTextInput } from '../inline-text-input';

describe('<InlineTextInput />', () => {
  it('renders the InlineTextInput correctly', () => {
    const comp = render(<InlineTextInput id="app" title="New Application:"/>);
    expect(comp.asFragment()).toMatchSnapshot();
  });
});
