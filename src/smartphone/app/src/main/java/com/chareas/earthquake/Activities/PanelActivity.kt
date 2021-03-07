package com.chareas.earthquake.Activities

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.chareas.earthquake.Adapters.BuildingAdapter
import com.chareas.earthquake.Models.Building
import com.chareas.earthquake.R
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber


class PanelActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var buildings: MutableList<Building> = mutableListOf()
    private lateinit var adapter: BuildingAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = "Buildings"
        findViewById<FloatingActionButton>(R.id.add_btn).setOnClickListener {
            if (isGPSEnabled()) {
                startActivity(Intent(this, AddAutopsy::class.java))
            } else {
                Snackbar.make(
                    it,
                    "Η τοποθεσία είναι απενεργοποιημένη, για να προσθέσετε κάποια αυτοψία χρειαζόμαστε την τοποθεσία σας. Παρακαλώ ενεργοποιήστε την και δοκιμάστε ξανά",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        database = Firebase.database.reference
//        initData()
        adapter = BuildingAdapter(buildings)


        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildAdded:" + dataSnapshot.key!!)

                // A new building has been added, add it to the displayed list
                var b = dataSnapshot.getValue(Building::class.java)!!
//                (buildings as MutableList<Building>).add(b)
                buildings.add(b)
                adapter.notifyDataSetChanged()


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
                        Timber.d("Updated list item $index")
                        return@forEachIndexed
                    }

                }
                adapter.notifyDataSetChanged()


                // ...
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Timber.d("onChildRemoved:" + dataSnapshot.key!!)

                // A building has changed, use the key to determine if we are displaying this
                // building and if so remove it.
                val buildingKey = dataSnapshot.key
                buildings.remove(dataSnapshot.getValue(Building::class.java))
                adapter.notifyDataSetChanged()

                // ...
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildMoved:" + dataSnapshot.key!!)

                // A building has changed position, use the key to determine if we are
                // displaying this building and if so move it.
                val movedbuilding = dataSnapshot.getValue(Building::class.java)
                val buildingKey = dataSnapshot.key

                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.d("postbuildings:onCancelled  ${databaseError.toException()}")
                Toast.makeText(
                    applicationContext, "Failed to load buildings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        database.child("buildings").orderByChild("registered")
            .addChildEventListener(childEventListener)

        val buildingsRecycler = findViewById<RecyclerView>(R.id.buildings_recycler)
        buildingsRecycler.adapter = adapter
        buildingsRecycler.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

//        buildingsRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}