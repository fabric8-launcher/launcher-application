import * as React from 'react';
import { Component } from 'react';

import './Console.scss';

interface ConsoleProps {
  name: string;
  content?: Array<(string | React.ReactNode)>;
}

interface ConsoleState {
  styleClass: string;
}

export class Console extends Component<ConsoleProps, ConsoleState> {

  private readonly ref = React.createRef<HTMLDivElement>();

  constructor(props: ConsoleProps) {
    super(props);
    this.state = {
      styleClass: 'no-content',
    };
  }

  public componentDidUpdate() {
    if (this.ref.current) {
      if (this.ref.current.lastChild) {
        const offset = (this.ref.current.lastChild as HTMLDivElement).offsetTop - this.ref.current.offsetTop;
        this.ref.current.scrollTop = offset;
      }
      const contentSize = this.props.content ? this.props.content.length : 0;

      if (contentSize === 0 && this.state.styleClass !== 'no-content') {
        this.setState({styleClass: 'no-content'});
      }
      if (contentSize === 1 && this.state.styleClass !== 'expanded') {
        this.setState({styleClass: 'expanded'});
      }
    }
  }

  public render() {
    const consoleContent = this.props.content || [];
    return (
      <div className={`console`} aria-label={`${this.props.name} Console`}>
        <div className={`console-content ${this.state.styleClass}`} ref={this.ref}>
          <span className="placeholder"><span className="prefix">>_</span>&nbsp;
            Use the 'Execute' button(s) above to get things started in this console...</span>
          {consoleContent.map((c, i) => (<div key={i} className="console-content-row">{c}</div>))}
        </div>
      </div>
    );
  }
}
