import * as React from 'react';
import CopyToClipboard from 'react-copy-to-clipboard';
import './ShellCommand.scss';
import { Button, Split, SplitItem } from '@patternfly/react-core';
import { ClipboardCheckIcon, ClipboardIcon } from '@patternfly/react-icons';

interface ShellCommandProps {
  readonly command: string;
  readonly onlyButton?: boolean;
  readonly buttonText?: string;
}

class ShellCommand extends React.Component<ShellCommandProps, any> {

  constructor(props: any) {
    super(props);

    this.state = {
      copied: 0
    };

    this.onCopy = this.onCopy.bind(this);
    this.onMouseOutOfCopy = this.onMouseOutOfCopy.bind(this);
  }

  public render() {
    const CopyToClipboardButton = () => (
      <CopyToClipboard
        text={this.props.command}
        onCopy={this.onCopy}
      >
        <Button
          className="copy-button"
          title={this.props.buttonText}
          onMouseLeave={this.onMouseOutOfCopy}
        >
          {this.state.copied ? <ClipboardCheckIcon/> : <ClipboardIcon/>}
        </Button>
      </CopyToClipboard>
    );
    if (this.props.onlyButton) {
      return (
        <CopyToClipboardButton/>
      );
    }

    return (
      <Split className="shell-command">
        <SplitItem isFilled={false}>
          <div className="shell-command-prefix">
            $
          </div>
        </SplitItem>
        <SplitItem isFilled={true}>
          <input
            type="text"
            className="shell-command-value"
            readOnly={true}
            placeholder={this.props.command}
          />
        </SplitItem>
        <SplitItem isFilled={false}>
          <CopyToClipboardButton/>
        </SplitItem>
      </Split>
    );
  }

  private onCopy() {
    this.setState({copied: 1});
  }

  private onMouseOutOfCopy() {
    this.setState({copied: 0});
  }

}

export default ShellCommand;
