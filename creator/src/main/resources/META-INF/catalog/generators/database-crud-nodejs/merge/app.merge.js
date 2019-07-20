
const db = require('./lib/db');
const fruits = require('./lib/routes/fruits');

app.use('/api', fruits);

db.init().then(() => {
  console.log('Database init\'d');
}).catch(err => {
  console.log(err);
});
