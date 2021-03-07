var buildingsRef = firebase.database().ref('buildings/');

buildingsRef.on('child_added', function(snapshot) {

  if ((snapshot.val().status).localeCompare('Κατοικήσιμο') == 0) {
    myPieChart.data.datasets[0].data[0] += 1;
  } else if ((snapshot.val().status).localeCompare('Μη Κατοικήσιμο') == 0) {
    myPieChart.data.datasets[0].data[1] += 1;
  }

  myChart.data.datasets[0].data[0] = (myPieChart.data.datasets[0].data[0] + myPieChart.data.datasets[0].data[1]);
  myChart.data.datasets[0].data[1] += snapshot.val().numberOfReviews;

  if (snapshot.hasChild("reviewedStatus")) {
    if ((snapshot.val().reviewedStatus).localeCompare('Κατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[0] += 1;
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο επικίνδυνο για χρήση') == 0) {
      myChart2.data.datasets[0].data[1] += 1;
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο προσωρινά ακατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[2] += 1;
    }
  }

  // console.log(myPieChart.data.datasets[0].data[0]);
  // console.log(myPieChart.data.datasets[0].data[1]);
  // console.log((myPieChart.data.datasets[0].data[0] + myPieChart.data.datasets[0].data[1]));

  myPieChart.update();
  myChart.update();
  myChart2.update();

});


buildingsRef.on('child_changed', function(snapshot) {

  if (snapshot.hasChild("reviewedStatus")) {
    if ((snapshot.val().reviewedStatus).localeCompare('Κατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[0] += 1
      //myChart.data.datasets[0].data[1] += 1;
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο επικίνδυνο για χρήση') == 0) {
      myChart2.data.datasets[0].data[1] += 1
      //myChart.data.datasets[0].data[1] += 1
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο προσωρινά ακατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[2] += 1
    //  myChart.data.datasets[0].data[1] += 1
    }
  }

  myChart.data.datasets[0].data[1] += 1;
  myChart.update();
  myChart2.update();
});


buildingsRef.on('child_removed', function(snapshot) {

  if ((snapshot.val().status).localeCompare('Κατοικήσιμο') == 0) {
    myPieChart.data.datasets[0].data[0] -= 1;
  } else if ((snapshot.val().status).localeCompare('Μη Κατοικήσιμο') == 0) {
    myPieChart.data.datasets[0].data[1] -= 1;
    if ((snapshot.val().reviewedStatus).localeCompare('Κατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[0] -= 1;
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο επικίνδυνο για χρήση') == 0) {
      myChart2.data.datasets[0].data[1] -= 1;
    } else if ((snapshot.val().reviewedStatus).localeCompare('Κτίριο προσωρινά ακατάλληλο για χρήση') == 0) {
      myChart2.data.datasets[0].data[2] -= 1;
    }
  }
  myPieChart.update();
  myChart2.update();
});

var ctx = document.getElementById('myChart');
var myChart = new Chart(ctx, {
  type: 'bar',
  data: {
    labels: ['ταχείες αυτοψίες', 'επανέλεγχοι'],
    datasets: [{
      label: '# επανελέγχων',
      data: [0, 0],
      backgroundColor: [
        'rgba(153, 102, 255, 0.2)',
        'rgba(54, 162, 235, 0.2)',
        'rgba(255, 206, 86, 0.2)',
        'rgba(75, 192, 192, 0.2)',
        'rgba(153, 102, 255, 0.2)',
        'rgba(255, 159, 64, 0.2)'
      ],
      borderColor: [
        'rgba(153, 102, 255, 1)',
        'rgba(54, 162, 235, 1)',
        'rgba(255, 206, 86, 1)',
        'rgba(75, 192, 192, 1)',
        'rgba(153, 102, 255, 1)',
        'rgba(255, 159, 64, 1)'
      ],
      borderWidth: 1
    }]
  },
  options: {
    scales: {
      yAxes: [{
        ticks: {
          beginAtZero: true
        }
      }]
    }
  }
});




var ctx = document.getElementById('myPieChart');
var myPieChart = new Chart(ctx, {
  type: 'pie',
  data: {
    labels: ['Κατοικήσιμα', 'Μη Κατοικήσιμα'],
    datasets: [{
      label: '# επανελέγχων',
      data: [0, 0],
      backgroundColor: [
        'rgba(46, 125, 50, 0.2)',
        'rgba(251, 192, 45, 0.2)',
      ],
      borderColor: [
        'rgba(46, 125, 50, 1)',
        'rgba(251, 192, 45, 1)',
      ],
      borderWidth: 1
    }]
  },
  options: {
    scales: {}
  }
});


var ctx = document.getElementById('myChart2');
var myChart2 = new Chart(ctx, {
  type: 'bar',
  data: {
    labels: ['Κτίριο Κατάλληλο για χρήση', 'Κτίριο επικίνδυνο για χρήση', 'Κτίριο προσωρινά ακατάλληλο για χρήση'],
    datasets: [{
      label: '# επανελέγχων',
      data: [0, 0, 0],
      backgroundColor: [
        'rgba(46, 125, 50, 0.2)',
        'rgba(251, 192, 45, 0.2)',
        'rgba(198, 40, 40, 0.2)',
        'rgba(75, 192, 192, 0.2)',
        'rgba(153, 102, 255, 0.2)',
        'rgba(255, 159, 64, 0.2)'
      ],
      borderColor: [
        'rgba(46, 125, 50, 1)',
        'rgba(251, 192, 45, 1)',
        'rgba(198, 40, 40, 1)',
        'rgba(75, 192, 192, 1)',
        'rgba(153, 102, 255, 1)',
        'rgba(255, 159, 64, 1)'
      ],
      borderWidth: 1
    }]
  },
  options: {
    scales: {
      yAxes: [{
        ticks: {
          beginAtZero: true
        }
      }]
    }
  }
});
