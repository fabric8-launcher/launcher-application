export function defaultIfEmpty(val: string | undefined, def: string): string {
  return val ? val : def;
}

export function capitalizeFirstLetter(val: string) {
  return val.charAt(0).toUpperCase() + val.slice(1);
}
