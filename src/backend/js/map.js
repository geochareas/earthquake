let map;
let lat;
let long;
let marker;
let markers = [];


let autopsies = $('#autopsies').DataTable();


function initMap() {

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

  buildingsRef.on('child_removed', function (snapshot) {

    //otan diagrafoume ena building apo thn vash
    //afairoume kai tis kartes
    $(`[building-id="${snapshot.key}"]`).remove();
    //vgazoume kai ta markers apo thn lista markers me to idio id
    markers.forEach(marker, index => {
      if (snapshot.key == marker.id) {
        markers.splice(index, 1);
      }
    });

    autopsies.row(`#${snapshot.val().id}`).remove().draw();

  });


  buildingsRef.on('child_changed', function (snapshot) {

    autopsies.row(`#${data.val().uid}`).data([
      snapshot.val().location.address,
      snapshot.val().firstName + ' ' + snapshot.val().lastName,
      snapshot.val().tel,
      `<a rel="tooltip" data-toggle="tooltip-${snapshot.key}" data-placement="right" data-html="true" title="${getStatusDescription(snapshot)}">
        <img class="status-icon" src="${getStatusIcon(snapshot)}"/>
        ${getStatusTitle(snapshot)}
      </a>`,
      date,
      `<button type="button" class="btn btn-primary text-capitalize" data-toggle="modal" data-target="#${snapshot.key}">Περισσότερες πληροφορίες</button>`,
    ]).draw();

    // console.log(snapshot.val().images.length);
    if (snapshot.hasChild("images")) {
      console.log("test");
      snapshot.val().images.map((image, index) => {
        $(`<div class="carousel-item">
      <img class="d-block w-100" src="${image}" alt="Second slide">
      </div>`).appendTo(`#carousel-${snapshot.key} .carousel-inner`);

        $(`<li data-target="#carousel-${snapshot.key}" data-slide-to="${index}">
      </li>`).appendTo(`#carousel-${snapshot.key} .carousel-indicators`);
      });
    }


    $(`#carousel-${snapshot.key} .carousel-item`).first().addClass('active');
    $(`#carousel-${snapshot.key} .carousel-indicators > li`).first().addClass('active');
    $(`#carousel-${snapshot.key}`).carousel();
  });



  buildingsRef.on('child_added', function (snapshot) {

    addMarker(snapshot);


    let date = moment(snapshot.val().registered).format('LLLL');

    autopsies.row.add([
      snapshot.val().location.address,
      snapshot.val().firstName + ' ' + snapshot.val().lastName,
      snapshot.val().tel,
      `<a rel="tooltip" data-toggle="tooltip-${snapshot.key}" data-placement="right" data-html="true" title="${getStatusDescription(snapshot)}">
      <img class="status-icon" src="${getStatusIcon(snapshot)}"/>
      ${getStatusTitle(snapshot)}
    </a>`,
      date,
      `<button type="button" class="btn btn-primary text-capitalize" data-toggle="modal" data-target="#${snapshot.key}">Περισσότερες πληροφορίες</button>`,
    ]).node().id = `${snapshot.val().id}`;
    autopsies.draw();

    // $('#autopsies').prepend(`
    //     <tr>
    //       <td>${snapshot.val().location.address}</td>
    //       <td>${snapshot.val().firstName + ' ' + snapshot.val().lastName}</td>
    //       <td>${snapshot.val().tel}</td>
    //       <td>

    //       </td>
    //       <td>${date}</td>
    //       <td>
    //         <button type="button" class="btn btn-primary text-capitalize" data-toggle="modal" data-target="#${snapshot.key}">Περισσότερες πληροφορίες</button>
    //       </td>
    //     </tr>
    // `);

    $('#card').prepend(`
    <div class="card ripple my-3" data-mdb-ripple-color="light" building-id='${snapshot.key}'>
      <h5 class="card-header">${snapshot.val().location.address}</h5>
      <div class="card-body">
      <h5 class="card-title">

        <a rel="tooltip" data-toggle="tooltip-${snapshot.key}" data-placement="right" data-html="true" title="${getStatusDescription(snapshot)}">
          <img class="status-icon" src="${getStatusIcon(snapshot)}"/>
          ${getStatusTitle(snapshot)}
        </a>
        </h5>
        <p class="card-text">Ιδιοκτήτης κτιρίου: ${snapshot.val().firstName + ' ' + snapshot.val().lastName}</p>
        <p class="card-text">Τηλέφωνο: ${snapshot.val().tel}</p>
        <p class="card-text">Ημερομηνία/ώρα εγγραφής: ${date}
         </p>
        <button type="button" class="btn btn-primary text-capitalize" data-toggle="modal" data-target="#${snapshot.key}">Περισσότερες πληροφορίες</button>
      </div>
    </div>

    <div class="modal fade bd-example-modal-lg" id="${snapshot.key}" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="container-fluid">

            <div class="row">
              <div class="col-md-4 ml-auto">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                  <span aria-hidden="true">&times;</span>
                </button>
              </div>
            </div>

            <div class="row p-2" id="data-${snapshot.key}">
            <div class="col-md-6">
            <div id="carousel-${snapshot.key}" class="carousel slide" data-ride="carousel">
              <div class="carousel-inner">


              </div>
              <a class="carousel-control-prev" href="#carousel-${snapshot.key}" role="button" data-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="sr-only">Previous</span>
              </a>
              <a class="carousel-control-next" href="#carousel-${snapshot.key}" role="button" data-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="sr-only">Next</span>
              </a>
            </div>
          </div>
              <div class="col-md-6 p-2">
                <form>

                  <h5 class="text-center pb-2">Στοιχεία ιδιοκτήτη</h5>
                  <hr>
                  <div class="form-row">
                    <div class="form-group col-md-12">
                      <label>Ονοματεπώνυμο: ${snapshot.val().firstName + ' ' + snapshot.val().lastName}</label>
                    </div>
                    <div class="form-group col-md-12">
                      <label>Τηλέφωνο: ${snapshot.val().tel}</label>
                    </div>
                  </div>
                  <h5 class="text-center pb-2">Πληροφορίες κτιρίου</h5>
                  <hr>
                  <div class="form-row">
                    <div class="form-group col-md-12">
                      <label>Διεύθυνση: ${snapshot.val().location.address}</label>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group col-md-6">
                      <label>Όροφοι: ${snapshot.val().floors}</label>
                    </div>
                    <div class="form-group col-md-6">
                      <label>Διαμερίσματα: ${snapshot.val().apartments}</label>
                    </div>
                    <div class="form-group col-md-12">
                      <label>Τύπος: ${snapshot.val().type}</label>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group col-md-12">
                      <label>Κατάσταση: ${snapshot.val().status}</label>
                    </div>
                    <div class="form-group col-md-12">
                      <label>Ημερομηνία/ώρα εγγραφής: ${date}</label>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group col-md-12">
                      <label>Παρατηρήσεις μηχανικού: ${snapshot.val().details}</label>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>`);

    if (snapshot.hasChild("images")) {

      snapshot.val().images.map((image, index) => {
        $(`<div class="carousel-item">
      <img class="d-block w-100" src="${image}" alt="Second slide">
      </div>`).appendTo(`#carousel-${snapshot.key} .carousel-inner`);

        $(`<li data-target="#carousel-${snapshot.key}" data-slide-to="${index}">
      </li>`).appendTo(`#carousel-${snapshot.key} .carousel-indicators`);
      });
    }


    $(`#carousel-${snapshot.key} .carousel-item`).first().addClass('active');
    $(`#carousel-${snapshot.key} .carousel-indicators > li`).first().addClass('active');
    $(`#carousel-${snapshot.key}`).carousel();

    $('body').tooltip({
      selector: '[rel="tooltip"]'
    });


  });

  function addMarker(building) {



    var marker = new google.maps.Marker({

      icon: {
        url: getStatusIcon(building),
        scaledSize: new google.maps.Size(35, 35)
      },
      position: {
        lat: parseFloat(building.val().location.lat),
        lng: parseFloat(building.val().location.long)
      },
      map: map
    });
    //

    marker.info = new google.maps.InfoWindow({
      content: "<p>Τοποθεσία Κτιρίου: </br>" + marker.getPosition() + "</p>"
    });
    google.maps.event.addListener(marker, "mouseover", () => {
      marker.info.open(map, marker);
    });
    google.maps.event.addListener(marker, "mouseout", () => {
      marker.info.close(map, marker);
    });
    google.maps.event.addListener(marker, "click", () => {
      console.log(`#carousel-${building.key}`);
      $(`#${building.key}`).modal("show");
    });
  }

  function getStatusIcon(building) {
    if (building.val().reviewedStatus != "") {

      if (building.val().reviewedStatus == "Κατάλληλο για χρήση") {
        return "../icons/location_on_green.svg";
      } else if (building.val().reviewedStatus == "Κτίριο προσωρινά ακατάλληλο για χρήση") {
        return "../icons/location_on_yellow.svg";
      } else {
        return "../icons/location_on_red.svg";
      }
    } else {
      if (building.val().status == "Κατοικήσιμο") {
        return "../icons/wrong_location_green.svg";
      } else {
        return "../icons/wrong_location_yellow.svg";
      }
    }

  }

  function getStatusTitle(building) {
    if (building.val().reviewedStatus != "") {

      if (building.val().reviewedStatus == "Κατάλληλο για χρήση") {
        return "Κατάλληλο για χρήση";
      } else if (building.val().reviewedStatus == "Κτίριο προσωρινά ακατάλληλο για χρήση") {
        return "Κτίριο προσωρινά ακατάλληλο για χρήση";
      } else {
        return "Κτίριο επικίνδυνο για χρήση";
      }
    } else {
      if (building.val().status == "Κατοικήσιμο") {
        return "Κατοικήσιμο";
      } else {
        return "Μη Κατοικήσιμο";
      }
    }

  }

  function getStatusDescription(building) {
    if (building.val().reviewedStatus != "") {

      if (building.val().reviewedStatus == "Κατάλληλο για χρήση") {
        return "Έπειτα από επανέλεγχο, το κτίριο <em>κρίθηκε κατάλληλο για χρήση</em>";
      } else if (building.val().reviewedStatus == "Κτίριο προσωρινά ακατάλληλο για χρήση") {
        return "Έπειτα απο επανέλεγχο, το κτίριο κρίθηκε <em>προσωρινά ακατάλληλο για χρήση</em>";
      } else {
        return "Έπειτα απο επανέλεγχο, το κτίριο κρίθηκε <em>επικίνδυνο για χρήση</em>";
      }
    } else {
      if (building.val().status == "Κατοικήσιμο") {
        return "To κτίριο στην πρώτη αυτοψία κρίθηκε ως <em>Κατοικήσιμο</em>";
      } else {
        return "To κτίριο στην πρώτη αυτοψία κρίθηκε ως <em>Μη Κατοικήσιμο</em>";
      }
    }

  }
}