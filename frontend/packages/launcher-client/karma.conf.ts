module.exports = (config) => {
  config.set({
    frameworks: ['jasmine', 'karma-typescript'],
    files: [
      { pattern: 'src/**/*.ts' }
    ],
    preprocessors: {
      '**/*.ts': ['karma-typescript']
    },
    karmaTypescriptConfig: {
      tsconfig: './tsconfig.json'
    },
    reporters: ['mocha', 'karma-typescript'],
    browsers: ['ChromeHeadless'],
    singleRun: true
  });
};
