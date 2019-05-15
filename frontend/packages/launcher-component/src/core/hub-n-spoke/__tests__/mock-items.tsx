import { HubItem } from '..';
import * as React from 'react';

export const mockItems: HubItem[] = [
  {
    id: 'hub1',
    title: 'Hub1',
    overview: {
      component:  ({edit}) => (<p>this is hub 1 overview</p>),
    },
    form: {
      component: ({close}) => (<p>this is hub 1 edition form</p>),
    }
  },
  {
    id: 'hub2',
    title: 'Hub2',
    overview: {
      component: ({edit}) => (<p>this is hub 2 overview</p>),
    },
    form: {
      component: ({close}) => (<p>this is hub 2 edition form</p>),
    }
  },
  {
    id: 'hub3',
    title: 'Hub3',
    overview: {
      component: ({edit}) => (<p>this is hub 3 overview</p>),
      width: 'full',
    },
    form: {
      component: ({close}) => (<p>this is hub 3 edition form</p>),
    }
  }
];
