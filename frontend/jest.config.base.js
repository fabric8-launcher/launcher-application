module.exports = {
  roots: ['./src'],
  collectCoverage: true,
  clearMocks: true,
  coverageReporters: [
    "lcov"
  ],
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleNameMapper: {
    "\\.(scss)|(css)$": "identity-obj-proxy",
  },
  testMatch: [ "**/?(*.)+(spec).ts?(x)" ]
};