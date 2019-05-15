import React, { useState } from 'react';
import { Text, TextInput, TextVariants, TextInputProps } from '@patternfly/react-core';

import style from './inline-text-input.module.scss';
import { EditAltIcon } from '@patternfly/react-icons';

interface InlineTextInputProps extends TextInputProps {
  title: string;
  warning?: string;
}

export function InlineTextInput(props: InlineTextInputProps) {
  const [hint, setHint] = useState(false);
  return (
    <div className={style.title}>
      <Text component={TextVariants.h1}>{props.prefix}</Text>
      <TextInput {...props} type="text" onMouseEnter={() => setHint(!hint)} onMouseLeave={() => setHint(!hint)} />
      {hint && <EditAltIcon />}
      <span style={{display: 'block', color: '#f0ab00', paddingTop: '5px'}}>{props.warning}</span>
    </div>
  );
}
