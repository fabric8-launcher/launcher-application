'use strict';

const db = require('../db');

function find (id) {
  //{{if .databaseType==mysql}}
  return db.query('SELECT * FROM products WHERE id = ?', [id]);
  //{{else if .databaseType==postgresql}}
  //return db.query('SELECT * FROM products WHERE id = $1', [id]);
  //{{end}}
}

function findAll () {
  return db.query('SELECT * FROM products');
}

function create (name, stock) {
  //{{if .databaseType==mysql}}
  return db.query('INSERT INTO products (name, stock) VALUES (?, ?)', [name, stock]);
  //{{else if .databaseType==postgresql}}
  //return db.query('INSERT INTO products (name, stock) VALUES ($1, $2) RETURNING *', [name, stock]);
  //{{end}}
}

function update (options = {}) {
  //{{if .databaseType==mysql}}
  return db.query('UPDATE products SET name = ?, stock = ? WHERE id = ?', [options.name, options.stock, options.id]);
  //{{else if .databaseType==postgresql}}
  //return db.query('UPDATE products SET name = $1, stock = $2 WHERE id = $3 RETURNING *', [options.name, options.stock, options.id]);
  //{{end}}
}

function remove (id) {
  //{{if .databaseType==mysql}}
  return db.query('DELETE FROM products WHERE id = ?', [id]);
  //{{else if .databaseType==postgresql}}
  //  return db.query('DELETE FROM products WHERE id = $1', [id]);
  //{{end}}
}

module.exports = {
  find,
  findAll,
  create,
  update,
  remove
};
