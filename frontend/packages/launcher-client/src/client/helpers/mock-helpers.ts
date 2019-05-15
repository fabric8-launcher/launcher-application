export async function waitForTick(name?: string, delay?: number): Promise<void> {
  return new Promise(resolve => {
    if(name) {
      console.debug(`${name} will been called`);
    }
    const fn = () => {
      resolve();
      if(name) {
        console.debug(`${name} has been resolved`);
      }
    };
    delay ? setTimeout(fn, delay) : process.nextTick(fn);
  });
}
