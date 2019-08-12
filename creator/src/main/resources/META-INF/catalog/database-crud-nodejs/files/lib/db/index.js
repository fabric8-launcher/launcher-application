'use strict';

//{{if .databaseType==mysql}}
const mysql = require('mysql');
const pool = mysql.createConnection({
  host     : process.env.DB_HOST || 'localhost',
  user     : process.env.DB_USERNAME || 'user',
  password : process.env.DB_PASSWORD || 'password',
  database : 'my_data',
  multipleStatements: true
});

pool.connect(function(err) {
  if (err) {
    console.error('error connecting: ' + err.stack);
    return;
  }

  console.log('connected as id ' + pool.threadId);
});

//{{else if .databaseType==postgresql}}
// const {Pool} = require('pg');

// const serviceHost = process.env.DB_HOST || 'localhost';
// const user = process.env.DB_USERNAME || 'user';
// const password = process.env.DB_PASSWORD || 'password';
// const connectionString = `postgresql://${user}:${password}@${serviceHost}:5432/my_data`;

// const pool = new Pool({
//   connectionString
// });

//{{end}}

// -- Create the products table if not present
const initScript = `CREATE TABLE IF NOT EXISTS products (
  id        SERIAL PRIMARY KEY,
  name      VARCHAR(40) NOT NULL,
  stock     INT
);

DELETE FROM products;

INSERT INTO products (name, stock) values ('Apple', 10);
INSERT INTO products (name, stock) values ('Orange', 10);
INSERT INTO products (name, stock) values ('Pear', 10);`;

module.exports = {
  query: (text, params) => {
    //{{if .databaseType==mysql}}
    return new Promise(function(resolve, reject) {
      pool.query(text, params, function(error, results) {
        if (!error) {
          if (Array.isArray(results)) {
              resolve({rows: results, rowCount: results.length});
          } else {
              resolve({rows: [], rowCount: results.affectedRows, insertId: results.insertId});
          }
        } else {
          reject(error);
        }
      });
    });
    //{{else if .databaseType==postgresql}}
    // return pool.query(text, params);
    //{{end}}
  },
  init: () => {
    //{{if .databaseType==mysql}}
    return new Promise(function(resolve, reject) {
      pool.query(initScript, function(error) {
        if (!error) {
          resolve();
        } else {
          reject(error);
        }
      });
    });
    //{{else if .databaseType==postgresql}}
    // return pool.query(initScript);
    //{{end}}
  }
};
