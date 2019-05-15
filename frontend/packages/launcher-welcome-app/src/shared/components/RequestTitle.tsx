import { AngleDoubleRightIcon } from '@patternfly/react-icons';
import * as React from 'react';

export function RequestTitle(props: {
  children?: React.ReactNode
}) {
  return (
    <React.Fragment><AngleDoubleRightIcon className="with-text"/> {props.children}</React.Fragment>
  );
}
