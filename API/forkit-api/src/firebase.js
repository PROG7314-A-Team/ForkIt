const admin = require('firebase-admin');
const serviceAccount = require('../../../../ForkIt_Firebase_Credentials/firebase-service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

module.exports = admin;
