import { act } from '@testing-library/react';

export async function flushPromises() {
    await act(async () => {
        console.log('flushPromises()');
        jest.runAllTicks();
        jest.runOnlyPendingTimers();
        jest.runAllImmediates();
        await new Promise(process.nextTick);
    });
}
