// Your web app's Firebase configuration
var firebaseConfig = {
  apiKey: "AIzaSyByDURjozie1pgbfqeMiliRwygMOxNdBfk",
  authDomain: "qwerty-aegean.firebaseapp.com",
  projectId: "qwerty-aegean",
  storageBucket: "qwerty-aegean.appspot.com",
  messagingSenderId: "439364824767",
  appId: "1:439364824767:web:167b7fda88866db0bfe00a",
  databaseURL: "https://qwerty-aegean-default-rtdb.europe-west1.firebasedatabase.app"
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Get a reference to the database service
var database = firebase.database();

function createUser(name, email) {
  let userId = firebase.database().ref().child('users').push().key;
  database.ref('users/' + userId).set({
    "uid": userId,
    "displayName": name,
    "email": email,
    "civilEngineer": false,
    "photoUrl": "https://lh3.googleusercontent.com/a-/AOh14Ggkh0zWHq-w_KUZc5fk5ZvMdWVG_IUKyZkzsP4n3A=s96-c",
    "registered": new Date().getTime()
  });

  console.log(`Created user ${userId}`);
}
export {
  firebaseConfig,
  database,
  createUser
};
