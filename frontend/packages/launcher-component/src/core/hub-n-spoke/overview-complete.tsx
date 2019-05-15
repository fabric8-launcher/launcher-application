import { Title } from '@patternfly/react-core';
import { CheckCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import style from './hub-n-spoke.module.scss';

export function OverviewComplete(props: { id: string; title: string; children?: React.ReactNode }) {
  return (
    <div className={style.overviewComplete} aria-label={`${props.id} is configured`}>
      <Title size="lg" className={style.overviewCompleteTitle}>
        <CheckCircleIcon className={style.checkComplete}/>{props.title}
      </Title>
      {props.children && (<div className={style.overviewCompleteBody}>{props.children}</div>)}
    </div>
  );
}
