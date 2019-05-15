import { Button } from '@patternfly/react-core';
import { CaretSquareDownIcon, CaretSquareUpIcon } from '@patternfly/react-icons';
import React, { Fragment, ReactNode } from 'react';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';
import style from './toggle-panel.module.scss';


interface TogglePanelProps {
  title: string;
  children: ReactNode;
}

export function TogglePanel(props: TogglePanelProps) {
  const [collapse, setCollapse] = useSessionStorageWithObject(props.title, false);
  return (
    <Fragment>
      <div className={`${style.panel} ${(collapse ? style.expanded : '')}`}>
        {props.children}
      </div>
      <Button
        // @ts-ignore
        component="a"
        variant="link"
        aria-label="Expand panel"
        onClick={() => setCollapse(!collapse)}
      >
        {collapse ? (<span><CaretSquareUpIcon /> Fewer</span>) : (<span><CaretSquareDownIcon /> More</span>)} {props.title}
      </Button>
    </Fragment>
  );
}
