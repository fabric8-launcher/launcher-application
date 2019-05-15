import React, { ReactNode, Fragment } from 'react';
import { Button } from '@patternfly/react-core';
import { MinusCircleIcon, PlusCircleIcon } from '@patternfly/react-icons';

import style from './toggle-panel.module.scss';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';

interface TogglePanelProps {
  title: string;
  children: ReactNode;
}

export function TogglePanel(props: TogglePanelProps) {
  const [collapse, setCollapse] = useSessionStorageWithObject(props.title, false);
  return (
    <Fragment>
      <Button
        // @ts-ignore
        component="a"
        variant="link"
        aria-label="Expand panel"
        onClick={() => setCollapse(!collapse)}
      >
        {collapse ? <MinusCircleIcon /> : <PlusCircleIcon />} {props.title}
      </Button>
      <div className={`${style.panel} ${(collapse ? style.expanded : '')}`}>
        {props.children}
      </div>
    </Fragment>
  );
}
