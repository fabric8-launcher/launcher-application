export function checkNotNull<T>(param?: T, name : string = 'variable'): T {
  if (!param) {
    throw new Error(`${name} must be defined.`);
  }
  return param;
}