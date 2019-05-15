export function getLocationAbsoluteUrl(path: string = '') {
  const link = document.createElement('a');
  link.href = path;
  return link.href;
}

