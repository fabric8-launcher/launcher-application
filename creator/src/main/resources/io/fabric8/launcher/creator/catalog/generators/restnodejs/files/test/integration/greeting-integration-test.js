'use strict';

const test = require('tape');
const supertest = require('supertest');
const rhoaster = require('rhoaster');

const testEnvironment = rhoaster({
  deploymentName: 'nodejs-rest-http',
  nodeVersion: '10.x'
});

testEnvironment.deploy()
  .then(runTests)
  .then(_ => test.onFinish(testEnvironment.undeploy))
  .catch(console.error);

function runTests (route) {
  test('/api/greeting', t => {
    t.plan(1);
    supertest(route)
      .get('/api/greeting')
      .expect(200)
      .expect('Content-Type', /json/)
      .then(response => {
        t.equal(response.body.content, 'Hello, World!', 'should return the Hello, World! Greeting');
        t.end();
      });
  });

  test('/api/greeting with query param', t => {
    t.plan(1);
    supertest(route)
      .get('/api/greeting?name=luke')
      .expect(200)
      .expect('Content-Type', /json/)
      .then(response => {
        t.equal(response.body.content, 'Hello, luke', 'should return the Hello, luke Greeting');
        t.end();
      });
  });
}
