import { database } from './database.js';

firebase.auth().onAuthStateChanged((user) => {

  if (user) {
    var displayName = user.displayName.split(" ")[0];
    $('.welcome').text(`Καλως ηρθες, ${displayName}`);

  } else {
    // User is signed out
    window.location.href = "./login.html";
  }

});


$(document).ready(function() {


  $('body').bootstrapMaterialDesign();

  $('.logOut').click(function() {
    firebase.auth().signOut();
  });


});
