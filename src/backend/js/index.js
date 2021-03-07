// let map;
let map;
let lat;
let long;
let marker;
let markers = [];
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

firebase.auth().onAuthStateChanged((user) => {

  if (user) {
    var displayName = user.displayName.split(" ")[0];
    $('.welcome').text(`Καλως ηρθες, ${displayName}`);

    $('.welcome').removeClass('d-none');
    $('.signedInMenu').removeClass('d-none');
    $('.logIn').addClass('d-none');
  } else {
    // User is signed out
    $('.logIn').removeClass('d-none');
    $('.welcome').addClass('d-none');
    $('.signedInMenu').addClass('d-none');
  }

});

$('.logOut').click(function() {
  firebase.auth().signOut();
});


function initMap() {
  $('body').bootstrapMaterialDesign();


  const mapOptions = {
    zoom: 7,
    center: {
      lat: 38.288,
      lng: 23.281
    },
    styles: [{
        featureType: "administrative",
        elementType: "geometry",
        stylers: [{
          visibility: "off"
        }]
      },
      {
        featureType: "poi",
        elementType: "labels",
        stylers: [{
          visibility: "off"
        }]
      },
      {
        featureType: "road",
        elementType: "labels.icon",
        stylers: [{
          visibility: "off"
        }]
      },
      {
        featureType: "transit",
        stylers: [{
          visibility: "off"
        }]
      }
    ]
  };
  map = new google.maps.Map(document.getElementById("map"), mapOptions);

  var buildingsRef = firebase.database().ref('buildings/');
  buildingsRef.on("child_added", function(snapshot) {

    addMarker(snapshot.val().location);

  });

  buildingsRef.on('child_removed', function(snapshot) {
    //otan diagrafoume ena building apo thn vash
    //afairoume kai tis kartes
    $(`[building-id="${snapshot.key}"]`).remove();
    //vgazoume kai ta markers apo thn lista markers me to idio id
    markers.forEach(marker, index => {
      if (snapshot.key == marker.id) {
        markers.splice(index, 1);
      }
    });
  });

  function addMarker(location) {

    var marker = new google.maps.Marker({

      icon: `../icons/baseline_location_on_black_18dp.png`,
      position: {
        lat: parseFloat(location.lat),
        lng: parseFloat(location.long)
      },
      map: map
    });
  }


}
