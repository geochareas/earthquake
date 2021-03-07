package com.chareas.earthquake.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chareas.earthquake.Models.Building
import com.chareas.earthquake.Models.User
import com.chareas.earthquake.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private var user: FirebaseUser? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: AuthStateListener
    private lateinit var database: FirebaseDatabase
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    // A default location (Argyroupoli, Athens, Greece) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(37.9003247, 23.749)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.

    private val buildings: MutableList<Building> = mutableListOf()
    private val markers: MutableMap<String?, Marker> = mutableMapOf()
    private val buildingMarker: MutableMap<Marker, Building> = mutableMapOf()
    private lateinit var clickedMarkerLatLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(
                KEY_LOCATION
            )
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        auth = getInstance()
        user = auth.currentUser

        authListener = AuthStateListener { firebaseAuth ->

            user = firebaseAuth.currentUser
            if (user != null) {
                Timber.d("onAuthStateChanged: user has signed in (UID: ${user!!.uid})")

            } else { //user is not logged in
                Timber.d("onAuthStateChanged: user has signed out")

            }
            invalidateOptionsMenu()
        }

        auth.addAuthStateListener(authListener)

        database = Firebase.database


        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        Timber.plant(DebugTree())

    }


    /**
     * Saves the state of the map when the activity is paused.
     */

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(
                KEY_LOCATION,
                lastKnownLocation
            )
        }
        super.onSaveInstanceState(outState)
    }


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildAdded:" + dataSnapshot.key!!)

                // A new building has been added, add it to the displayed list
                var b = dataSnapshot.getValue(Building::class.java)!!
//                (buildings as MutableList<Building>).add(b)
                buildings.add(b)


                var marker = createMarker(b)
                markers[marker.id] = marker

                buildingMarker[marker] = b

                // ...
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildChanged: ${dataSnapshot.key}")

                // A building has changed, use the key to determine if we are displaying this
                // building and if so displayed the changed building.
                val newBuilding = dataSnapshot.getValue(Building::class.java)
                val buildingKey = dataSnapshot.key

                buildings.forEachIndexed { index, building ->

                    if (building.id == newBuilding!!.id) {
                        buildings[index] = newBuilding

                        for ((id, marker) in markers) {


                            if (marker.position.latitude == building!!.location["lat"]?.toDouble()
                                && marker.position.longitude == building!!.location["long"]?.toDouble()
                            ) {
                                marker.remove()
                                markers.remove(id)
                                buildingMarker.remove(marker)


                                var newMarker = createMarker(newBuilding)
                                markers[newMarker.id] = newMarker
                                buildingMarker[newMarker] = newBuilding
                                break

                            }
                        }
                        return@forEachIndexed
                    }

                }


                // ...
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Timber.d("onChildRemoved:" + dataSnapshot.key!!)


                var building = dataSnapshot.getValue(Building::class.java)
                buildings.remove(building)

                for ((id, marker) in markers) {
                    if (marker.position.latitude == building!!.location["lat"]?.toDouble()
                        && marker.position.longitude == building!!.location["long"]?.toDouble()
                    ) {
                        marker.remove()
                        markers.remove(id)
                        buildingMarker.remove(marker)
                        break
                    }
                }

                // ...
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildMoved:" + dataSnapshot.key!!)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.d("postbuildings:onCancelled  ${databaseError.toException()}")
                Toast.makeText(
                    applicationContext, "Failed to load buildings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        database.reference.child("buildings").orderByChild("registered")
            .addChildEventListener(childEventListener)

        map.setOnMarkerClickListener(this)
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json
            )
        )
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (user != null) {


            val userDataListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val user = dataSnapshot.getValue(User::class.java)

                    if (user!!.civilEngineer!!) {

                        buildingMarker.forEach { (currentMarker, building) ->

                            if (currentMarker == marker) {

                                val intent = Intent(applicationContext, ViewAutopsy::class.java)
                                intent.putExtra("building", building)
                                startActivity(intent)

                                return@forEach
                            }
                        }

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "You are not an engineer, you cannot access the panel yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.d("loadPost:onCancelled ${error.toException()}")
                    // ...
                }
            }
            database.getReference("users/" + user?.uid)
                .addListenerForSingleValueEvent(userDataListener)
        } else {
            Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show()
        }
        return false
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (user != null) {
            menuInflater.inflate(R.menu.menu_settings, menu)
        } else {
            menuInflater.inflate(R.menu.menu_login, menu)
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login -> {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                // Create and launch sign-in intent
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                    RC_SIGN_IN
                )
            }
            R.id.logout_button -> {
                auth.signOut()
                invalidateOptionsMenu()
            }
            R.id.panel_button -> {
                val userDataListener = object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val user = dataSnapshot.getValue(User::class.java)

                        if (user!!.civilEngineer) {
                            startActivity(Intent(applicationContext, PanelActivity::class.java))
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "You are not an engineer, you cannot access the panel yet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("loadPost:onCancelled ${error.toException()}")
                        // ...
                    }
                }
                Timber.d("${user?.uid}")
                database.getReference("users/" + user?.uid)
                    .addListenerForSingleValueEvent(userDataListener)
            }
        }


        return true
    }


    override fun onStart() {
        super.onStart()
        getDeviceLocation()


    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }


    // [START auth_fui_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = auth.currentUser

                if (response!!.isNewUser) {
                    val newUser = User(
                        user?.uid,
                        user?.email,
                        user?.displayName,
                        user?.photoUrl.toString(),
                        false,
                        user?.metadata?.creationTimestamp
                    )
                    database.getReference("users/" + user?.uid).setValue(newUser)

                }


                Toast.makeText(
                    applicationContext,
                    "Welcome back ${user?.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

            }
        }
    }


    // could be use used onPause() to prevent the application
    // from tracking the location in the background
    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Timber.d("Current location is null. Using defaults.")
                        Timber.e("Exception: ${task.exception}")
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }


                // as soon as we get the first location,
                // I assign a location listener to keep
                // track of the location status, so its real time
                locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval =
                    INTERVAL
                locationRequest.fastestInterval =
                    FASTEST_INTERVAL

                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: ${e.message} $e")
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }


    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Timber.e("Exception: ${e.message} - $e")
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // notify that location was changed
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined

        // if the distance between the two locations is noticeable (bigger than 0.1)
        if (lastKnownLocation != null && lastKnownLocation!!.distanceTo(location) > 0.1) {
            Timber.d("New location [${location.latitude}, ${location.longitude}]")

            // update the location
            lastKnownLocation = location

            // move the camera to the new location
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastKnownLocation!!.latitude,
                        lastKnownLocation!!.longitude
                    ), DEFAULT_ZOOM.toFloat()
                )
            )
        }
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createMarker(b: Building): Marker {


        var markerDrawable = if (b.reviewedStatus == "") {
            when (b.status) {
                "Κατοικήσιμο" -> resources.getDrawable(R.drawable.ic_baseline_wrong_location_green_24)
                else -> resources.getDrawable(R.drawable.ic_baseline_wrong_location_yellow_24)
            }

        } else {
            when (b.reviewedStatus) {
                "Κατάλληλο για χρήση" -> resources.getDrawable(R.drawable.ic_baseline_location_on_green_24)
                "Κτίριο προσωρινά ακατάλληλο για χρήση" -> resources.getDrawable(R.drawable.ic_baseline_location_on_yellow_24)
                else -> resources.getDrawable(R.drawable.ic_baseline_location_on_red_24)
            }
        }

        var markerIcon = getMarkerIconFromDrawable(markerDrawable)

        return map!!.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        b.location["lat"]!!.toDouble(),
                        b.location["long"]!!.toDouble()
                    )
                )
                .title("${b.firstName} ${b.lastName}'s Building ")
                .snippet(b.location["address"])
                .icon(markerIcon)
        )

    }

    companion object {

        var lastKnownLocation: Location? = null
        private const val RC_SIGN_IN = 123

        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        private val INTERVAL: Long = 2000
        private val FASTEST_INTERVAL: Long = 1000

    }
}