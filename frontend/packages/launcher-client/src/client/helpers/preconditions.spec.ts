import { checkNotNull } from './preconditions';

describe('Preconditions tools', () => {
  it('should throw error when null', () => {
    expect(() => checkNotNull(null, 'super')).toThrow('super must be defined.');
  });

  it('should not throw when not null', () => {
    const value = 'not null for sure';
    expect(checkNotNull(value)).toBe(value);
  });
});
