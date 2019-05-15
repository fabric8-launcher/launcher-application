declare module '*.json' {
  const value: any;
  export default value;
}
declare module '*.svg';
declare module '*.png';
declare module '*.jpg';

declare module '*.module.scss' {
  const content: {[className: string]: string};
  export = content;
}

declare namespace jest {
  interface Matchers<R> {
    toHaveAttribute: (attr: string, value?: string) => R
    toHaveTextContent: (htmlElement: string) => R
    toHaveClass: (className: string) => R
    toBeInTheDOM: () => R
  }
}
