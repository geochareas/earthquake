import {
  database
} from './database.js';


let users = $('#users').DataTable();


function addUser(data) {

  let date = moment(data.val().registered).format('LLLL');
  users.row.add([
    `<img class="img-fluid rounded-circle" src="${data.val().photoUrl}" alt=""/>`,
    // data.val().uid,
    data.val().displayName,
    data.val().email,
    data.val().civilEngineer,
    date,
    `<a href="#" user-id="${data.val().uid}"><i class="material-icons">supervisor_account</i></a>`
  ]).node().id = `${data.val().uid}`;
  users.draw();
  $(`[user-id="${data.val().uid}"]`).click(function() {
    let isEngineer;
    if (data.val().civilEngineer) {
      isEngineer = false;
    } else {
      isEngineer = true;
    }

    database.ref(`users/${data.val().uid}`).set({
      "uid": data.val().uid,
      "displayName": data.val().displayName,
      "email": data.val().email,
      "civilEngineer": isEngineer,
      "photoUrl": data.val().photoUrl,
      "registered": data.val().registered
    });

  });
}

function updateUser(data) {
  let date = moment(data.val().registered).format('LLLL');
  users.row(`#${data.val().uid}`).data([
    `<img class="img-thumbnail rounded-circle" src="${data.val().photoUrl}" alt=""/>`,
    // data.val().uid,
    data.val().displayName,
    data.val().email,
    data.val().civilEngineer,
    date,
    `<a href="#" user-id="${data.val().uid}"><i class="material-icons">supervisor_account</i></a>`
  ]).draw();
  $(`[user-id="${data.val().uid}"]`).click(function() {
    let isEngineer;
    if (data.val().civilEngineer) {
      isEngineer = false;
    } else {
      isEngineer = true;
    }

    database.ref(`users/${data.val().uid}`).set({
      "uid": data.val().uid,
      "displayName": data.val().displayName,
      "email": data.val().email,
      "civilEngineer": isEngineer,
      "photoUrl": data.val().photoUrl,
      "registered": data.val().registered
    });

  });
}

function deleteUser(data) {
  users.row(`#${data.val().uid}`).remove().draw();
}

export {
  addUser,
  updateUser,
  deleteUser
};
