import * as React from 'react';
import { Fragment, ReactElement, useContext, useState } from 'react';
import { Alert, AlertActionCloseButton, AlertVariant, Button, Grid, GridItem, Text, TextVariants } from '@patternfly/react-core';
import { EditIcon, WindowCloseIcon } from '@patternfly/react-icons';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';

import style from './hub-n-spoke.module.scss';
import { AlertError, Loader, optionalBool } from '../stuff';

export interface HubItem {
  id: string;
  title: string;
  visible?: boolean;
  loading?: boolean;
  overview: {
    component: (props: { edit: () => void }) => ReactElement<any>;
    width?: 'third' | 'half' | 'full';
  };
  form?: {
    component: (props: { close: () => void }) => ReactElement<any>;
  };
}

interface Hub {
  selected?: HubItem;

  open(item: HubItem);

  close();
}

const width = {
  quarter: 3,
  third: 4,
  half: 6,
  full: 12,
};

export const HubContext = React.createContext<Hub | undefined>(undefined);

export function HubOverviewCard(props: HubItem) {
  const hub = useContext(HubContext);
  const w = props.overview.width || 'quarter';
  const size = width[w];

  const onEdit = () => {
    if (hub) {
      hub.open(props);
    }
  };
  const loading = optionalBool(props.loading, false);
  return (
    // @ts-ignore
    <GridItem className="hub-and-spoke-item" sm={Math.min(size * 2, 12)} md={size}>
      <div className="hub-and-spoke-header">
        <h1>
          {props.title}
        </h1>
        <div className="hub-and-spoke-nav">
          {!loading && props.form && (
            <Button variant="plain" aria-label={`Open ${props.id} editor`} onClick={onEdit}>
              <EditIcon/>
            </Button>
          )}
        </div>
      </div>
      <div className="hub-and-spoke-body">
        {loading ? <Loader aria-label={`Loading ${props.id}`} />: props.overview.component({edit: onEdit})}
      </div>
    </GridItem>
  );
}

interface HubFormCardProps {
  title: string;
  id: string;
  children: React.ReactNode;
}

function HubFormCard(props: HubFormCardProps) {
  const hub = useContext(HubContext);
  const onClose = () => {
    if (hub) {
      hub.close();
    }
  };
  return (
    <GridItem className="hub-and-spoke-item hub-and-spoke-item-form" span={12} aria-label={`Edit ${props.id}`}>
      <div className="hub-and-spoke-header">
        <h1>
          {props.title}
        </h1>
        <div className="hub-and-spoke-nav">
          <Button variant="plain" aria-label={`Close ${props.id} editor`} onClick={onClose}>
            <WindowCloseIcon/>
          </Button>
        </div>
      </div>
      <div className="hub-and-spoke-body">
        {props.children}
      </div>
    </GridItem>
  );
}

interface HubAndSpokeProps {
  title: string | React.ReactNode;
  items: HubItem[];
  toolbar?: React.ReactNode;
  error?: any;
  hint?: string;
}

export function Hint(props: { value: string }) {
  const [visible, setVisible] = useSessionStorageWithObject<boolean>('hint', true);
  return (
    <Fragment>
      {visible && (
        <Alert
          variant={AlertVariant.info}
          title="What should I do?"
          aria-label="Hint"
          style={{margin: '40px'}}
          action={<AlertActionCloseButton onClose={() => setVisible(false)}/>}
        >
          {props.value}
        </Alert>
      )}
    </Fragment>
  );
}

export function HubNSpoke(props: HubAndSpokeProps) {
  const [selectedHub, setSelectedHub] = useState<HubItem | undefined>(undefined);

  const hub: Hub = {
    selected: selectedHub,
    open: (item: HubItem) => {
      if (item.form) {
        setSelectedHub(item);
      }
    },
    close: () => {
      setSelectedHub(undefined);
    },
  };

  return (
    <div className={style.hubNSpoke}>
      <HubContext.Provider value={hub}>
        {typeof props.title === 'string' &&
        <Text component={TextVariants.h1} className="hub-and-spoke-title">{props.title}</Text>}
        {typeof props.title !== 'string' && props.title}
        {!hub.selected && props.error && (
          <AlertError error={props.error}/>
        )}
        {!hub.selected && props.hint && !props.error && (
          <Hint value={props.hint}/>
        )}
        <Grid className="hub-and-spoke-container" gutter={'sm'}>
          {hub.selected ? (
            <HubFormCard id={hub.selected.id} title={hub.selected.title}>
              {hub.selected.form!.component({close: hub.close})}
            </HubFormCard>
          ) : props.items.filter(i => i.visible === undefined || i.visible)
            .map((item, i) => (<HubOverviewCard {...item} key={i}/>))}
        </Grid>
        {!hub.selected && props.toolbar}
      </HubContext.Provider>
    </div>
  );
}
