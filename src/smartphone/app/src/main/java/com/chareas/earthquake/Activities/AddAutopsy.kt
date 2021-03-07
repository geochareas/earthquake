package com.chareas.earthquake.Activities

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.chareas.earthquake.Adapters.BuildingImageAdapter
import com.chareas.earthquake.Models.Building
import com.chareas.earthquake.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import java.util.*


class AddAutopsy : AppCompatActivity() {

    private lateinit var buildingImages: GridView
    private lateinit var geocoder: Geocoder
    private lateinit var typeField: TextInputLayout// building type
    private lateinit var statusField: TextInputLayout // building status
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var location: MutableMap<String, String>
    private lateinit var buildingImageAdapter: BuildingImageAdapter
    private lateinit var imageUris: MutableList<Uri>
    private lateinit var uploadedUris: MutableList<Uri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_autopsy)
        geocoder = Geocoder(this, Locale.getDefault())
        database = Firebase.database.reference
        storage = Firebase.storage.reference.child("images")

        val addresses = geocoder.getFromLocation(
            MainActivity.lastKnownLocation!!.latitude,
            MainActivity.lastKnownLocation!!.longitude,
            1
        )

        var addressField = findViewById<TextInputEditText>(R.id.address)
        Timber.d("Address: ${addresses[0]}")

        val address = addresses[0].getAddressLine(0)
        val city = addresses[0].locality
        val state = addresses[0].adminArea
        val country = addresses[0].countryName
        val postalCode = addresses[0].postalCode

        imageUris = mutableListOf()
        uploadedUris = mutableListOf()

        location = mutableMapOf(
            "address" to address,
            "city" to city,
            "state" to state,
            "country" to country,
            "postalCode" to postalCode,
            "lat" to MainActivity.lastKnownLocation!!.latitude.toString(),
            "long" to MainActivity.lastKnownLocation!!.longitude.toString()
        )

        addressField.setText(address)

        val firstName = findViewById<TextInputEditText>(R.id.first_name)
        var lastName = findViewById<TextInputEditText>(R.id.last_name)
        var tel = findViewById<TextInputEditText>(R.id.tel)
        var floors = findViewById<TextInputEditText>(R.id.floors)
        var apartments = findViewById<TextInputEditText>(R.id.apartments)
        var details = findViewById<TextInputEditText>(R.id.details)

        val firstNameLayout = findViewById<TextInputLayout>(R.id.fn_layout)
        var lastNameLayout = findViewById<TextInputLayout>(R.id.ln_layout)
        var telLayout = findViewById<TextInputLayout>(R.id.tel_layout)
        var floorsLayout = findViewById<TextInputLayout>(R.id.floors_layout)
        var apartmentsLayout = findViewById<TextInputLayout>(R.id.apts_layout)
        var detailsLayout = findViewById<TextInputLayout>(R.id.det_layout)

        typeField = findViewById(R.id.type)
        var typeItems = listOf(
            "Κατοικία σε χρήση",
            "Κατοικία εγκαταλελειμένη",
            "Σταύλος/Αποθήκη",
            "Επαγγελματικός χώρος",
            "Σχολείο",
            "Ξενοδοχείο"
        )
        var adapter = ArrayAdapter(
            this,
            R.layout.list_item, typeItems
        )

        var actv = (typeField.editText as? AutoCompleteTextView)
        actv?.setAdapter(adapter)
        actv?.setText(actv.adapter.getItem(0).toString(), false)

        statusField = findViewById(R.id.status)

        val statusItems = listOf("Κατοικήσιμο", "Μη Κατοικήσιμο")
        adapter = ArrayAdapter(
            this,
            R.layout.list_item, statusItems
        )
        actv = (statusField.editText as? AutoCompleteTextView)
        actv?.setAdapter(adapter)
        actv?.setText(actv.adapter.getItem(0).toString(), false)


        val saveBtn = findViewById<Button>(R.id.btn_save)
        saveBtn.setOnClickListener {

            if (!isEmpty(firstName)
                && !isEmpty(lastName)
                && !isEmpty(tel)
                && !isEmpty(floors)
                && !isEmpty(apartments)
            ) {
                val key = database.child("buildings").push().key
                var building = Building(
                    key,
                    location,
                    firstName.text.toString(),
                    lastName.text.toString(),
                    tel.text.toString(),
                    floors.text.toString().toIntOrNull(),
                    apartments.text.toString().toIntOrNull(),
                    typeField.editText?.text.toString(),
                    statusField.editText?.text.toString(),
                    details.text.toString(),
                    "",
                    0,
                    mutableListOf()
                )

                database.child("buildings/" + key).setValue(building)
                Toast.makeText(applicationContext, "Η προσθήκη ήταν επιτυχής", Toast.LENGTH_SHORT).show()
                finish()

            } else {
                if (isEmpty(firstName)) {
                    firstNameLayout.error = "Το Όνομα είναι υποχρεωτικό"
                } else {
                    firstNameLayout.error = null
                }

                if (isEmpty(lastName)) {
                    lastNameLayout.error = "Το Επώνυμο είναι υποχρεωτικό"
                } else {
                    lastNameLayout.error = null
                }

                if (isEmpty(tel)) {
                    telLayout.error = "Το Τηλέφωνο είναι υποχρεωτικό"
                } else {
                    telLayout.error = null
                }

                if (isEmpty(floors)) {
                    floorsLayout.error = "Ο Αριθμός Ορόφων είναι υποχρεωτικός"
                } else {
                    floorsLayout.error = null
                }

                if (isEmpty(apartments)) {
                    apartmentsLayout.error = "Ο Αριθμός Διαμερισμάτων είναι υποχρεωτικός"
                } else {
                    apartmentsLayout.error = null
                }
                Snackbar.make(
                    it,
                    "Υπάρχουν άδεια υποχρεωτικά πεδία. Παρακαλώ συμπληρώστε τα για να συνεχίσετε.",
                    Snackbar.LENGTH_SHORT
                ).show()

            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            imageUris.add(photoUri)
            Timber.d("Added photo: $photoUri")

            buildingImageAdapter = BuildingImageAdapter(
                this,
                imageUris
            )
            buildingImages.adapter = buildingImageAdapter


        }

    }

    private fun isEmpty(field: TextInputEditText): Boolean {
        if (field.text.toString().trim().isNotEmpty())
            return false
        return true
    }

    companion object {
        lateinit var photoUri: Uri
    }

}