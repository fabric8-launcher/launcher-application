import * as React from 'react';

import { DataList, DataListCell, DataListItem, Modal, DataListItemRow } from '@patternfly/react-core';
import { ErrorCircleOIcon, InProgressIcon, OkIcon, PauseCircleIcon } from '@patternfly/react-icons';
import { Loader, Spin } from '@launcher/component';

interface EventStatus {
  statusMessage: string;
  data?: {
    location?: string;
    error?: string;
  };
}

interface ProcessingAppProps {
  progressEvents?: Array<{ name: string, message: string }>;
  progressEventsResults?: EventStatus[];
}

function StatusIcon(props: { status: Statuses }) {
  switch (props.status) {
    case 'in-progress':
      return (<Spin><InProgressIcon /></Spin>);
    case 'paused':
      return (<PauseCircleIcon />);
    case 'completed':
      return (<OkIcon style={{ color: '#80D228' }} />);
    case 'in-error':
      return (<ErrorCircleOIcon />);
    default:
      throw new Error(`Invalid status ${props.status}`);
  }
}

type Statuses = 'in-progress' | 'completed' | 'in-error' | 'paused';

function Popup(props) {
  return (
    <Modal
      title="Launch in progress..."
      isOpen
    >
      {props.children}
    </Modal>);
}

export function ProcessingApp(props: ProcessingAppProps) {

  if (!props.progressEvents) {
    return (<Popup><Loader aria-label="Waiting for server response..." /></Popup>);
  }

  const progressSteps: Array<{ id: number, name: string, message: string, status: Statuses }> = new Array(4);
  const length = props.progressEvents && (props.progressEvents.length || 0);
  const getStatus = (eventName: string) => {
    return props.progressEventsResults && props.progressEventsResults.find(s => s.statusMessage === eventName);
  };
  for (let i = 0; i < length; i++) {
    const step = props.progressEvents![i]!;
    const status = getStatus(step.name);
    progressSteps[i] = {
      id: i,
      name: step.name,
      message: step.message,
      status: status ? (status.data && status.data.error ? 'in-error' : 'completed') : 'in-progress',
    };
  }

  const ProgressEvent = ({ event }) => (
    <DataListItem aria-labelledby={`progress-event-${event.name}`} isExpanded={false}>
      <DataListItemRow>
        <DataListCell width={1}><StatusIcon status={event.status} /></DataListCell>
        <DataListCell
          width={4}
          id={`progress-event-${event.name}`}
          aria-label={`${event.name} is ${event.status}`}
        >
          {event.message}
        </DataListCell>
      </DataListItemRow>
    </DataListItem>
  );

  return (
    <Popup>
      <DataList aria-label="Receiving launch progress events...">
        {progressSteps.map(s => (<ProgressEvent key={s.id} event={s} />))}
      </DataList>
    </Popup>
  );
}
