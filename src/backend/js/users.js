import {
    addUser,
    updateUser,
    deleteUser
} from './functions.js';


var usersRef = firebase.database().ref('users');


$(document).ready(function () {

    usersRef.on('child_added', addUser);
    usersRef.on('child_changed', updateUser);
    usersRef.on('child_removed', deleteUser);

});