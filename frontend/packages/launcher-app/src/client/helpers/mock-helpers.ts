export async function promiseWithDelay(name?: string, delay: number = 0): Promise<void> {
  return new Promise(resolve => {
    if(name) {
      console.debug(`${name} will be called in ${delay}`);
    }
    const fn = () => {
      resolve();
      if(name) {
        console.debug(`${name} has been resolved`);
      }
    };
    setTimeout(fn, delay);
  });
}
