package com.chareas.earthquake.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
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

class ViewAutopsy : AppCompatActivity() {

    private lateinit var buildingImages: GridView
    private lateinit var typeField: TextInputLayout// building type
    private lateinit var statusLabel: TextView // building status
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var buildingImageAdapter: BuildingImageAdapter
    private lateinit var imageUris: MutableList<Uri>
    private lateinit var uploadedUris: MutableList<Uri>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_autopsy)


        val building = intent.getParcelableExtra<Building>("building")
        Timber.d("Received: $building")
        database = Firebase.database.reference
        storage = Firebase.storage.reference.child("images")


        var addressField = findViewById<TextInputEditText>(R.id.address)
        var firstName = findViewById<TextInputEditText>(R.id.first_name)
        var lastName = findViewById<TextInputEditText>(R.id.last_name)
        var tel = findViewById<TextInputEditText>(R.id.tel)
        var floors = findViewById<TextInputEditText>(R.id.floors)
        var apartments = findViewById<TextInputEditText>(R.id.apartments)
        var details = findViewById<TextInputEditText>(R.id.details)


        imageUris = mutableListOf()
        uploadedUris = mutableListOf()

        this.title = "Κτίριο ${building.firstName} ${building.lastName}"
        addressField.setText(building.location["address"])


        firstName.setText(building.firstName)
        lastName.setText(building.lastName)
        tel.setText(building.tel)
        floors.setText(building.floors.toString())
        apartments.setText(building.apartments.toString())
        details.setText(building.details)

        typeField = findViewById(R.id.type)
        var typeItems = mutableListOf(
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
        (typeField.editText as? AutoCompleteTextView)?.setAdapter(adapter)



        typeItems.forEachIndexed { index, type ->

            if (type == building.type) {

                var actv = (typeField.editText as? AutoCompleteTextView)
                actv!!.setText(
                    actv!!.adapter.getItem(index).toString(),
                    false
                )
                return@forEachIndexed
            }
        }


        statusLabel = findViewById(R.id.statusLabel)
        statusLabel.text = building.status
        var statusDrawable = when (building.status) {
            "Κατοικήσιμο" -> resources.getDrawable(R.drawable.ic_baseline_close_24)
            else -> resources.getDrawable(R.drawable.ic_baseline_change_history_24)
        }
        statusLabel.setCompoundDrawablesWithIntrinsicBounds(statusDrawable, null, null, null)


        var reviewedStatusField = findViewById<TextInputLayout>(R.id.reviewedStatus)
        if (building.status == "Μη Κατοικήσιμο") {
            var statusItems = listOf(
                "Κατάλληλο για χρήση",
                "Κτίριο προσωρινά ακατάλληλο για χρήση",
                "Κτίριο επικίνδυνο για χρήση"
            )
            reviewedStatusField.visibility = View.VISIBLE

            var reviewedAdapter = ArrayAdapter(
                this,
                R.layout.list_item, statusItems
            )
            (reviewedStatusField.editText as? AutoCompleteTextView)?.setAdapter(reviewedAdapter)

            if (building.reviewedStatus != "") {
                statusItems.forEachIndexed { index, item ->

                    if (building.reviewedStatus == item) {

                        var actv = (reviewedStatusField.editText as? AutoCompleteTextView)
                        actv?.setText(actv.adapter.getItem(index).toString(), false)
                        return@forEachIndexed
                    }
                }
            }

        }

        imageUris = building.images?.toList()?.map { it.toUri() } as MutableList<Uri>
        buildingImages = findViewById(R.id.building_images)
        buildingImageAdapter = BuildingImageAdapter(
            this,
            imageUris
        )
        buildingImages.adapter = buildingImageAdapter

        val saveBtn = findViewById<Button>(R.id.btn_save)
        saveBtn.setOnClickListener {
            if (building.reviewedStatus != reviewedStatusField.editText?.text.toString()) {
                building.numberOfReviews = building.numberOfReviews?.plus(1)
            }

            var building = Building(
                building.id,
                building.location,
                firstName.text.toString(),
                lastName.text.toString(),
                tel.text.toString(),
                floors.text.toString().toIntOrNull(),
                apartments.text.toString().toIntOrNull(),
                typeField.editText?.text.toString(),
                building.status,
                details.text.toString(),
                reviewedStatusField.editText?.text.toString(),
                building.numberOfReviews,
                building.images,
                building.registered
            )


            database.child("buildings/" + building.id).setValue(building)

            if (imageUris.isNotEmpty()) { // means that user didn't add any new images

                imageUris.forEach { uri ->
                    val fileRef = storage.child("images/${uri.lastPathSegment}")


                    // Register observers to listen for when the upload is done or if it fails
                    fileRef.putFile(uri).continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { e ->
                                throw e
                            }
                        }
                        fileRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result

                            uploadedUris.add(downloadUri!!)

                            building.images =
                                building.images?.plus(downloadUri.toString())


                            database.child("buildings/" + building.id).setValue(building)

                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                }
            }


            Snackbar.make(it, "Updated autopsy", Snackbar.LENGTH_SHORT).show()
            finish()

        }

        val deleteBtn = findViewById<Button>(R.id.btn_del)
        deleteBtn.setOnClickListener {
            // Initialize a new instance of
            val builder = AlertDialog.Builder(this@ViewAutopsy)

            // Set the alert dialog title
            builder.setTitle("Διαγραφή Φακέλου")

            // Display a message on alert dialog
            builder.setMessage("Είστε σίγουρος/η ότι θέλετε να διαγράψετε αυτό το φάκελο?")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Ναι") { _, _ ->
                // Do something when user press the positive button

                database.child("buildings/" + building.id).removeValue().addOnSuccessListener {
                    Snackbar.make(
                        deleteBtn,
                        "O φάκελοσ διαγράφηκε",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }


            // Display a neutral button on alert dialog
            builder.setNeutralButton("Ακύρωση") { _, _ ->
                Snackbar.make(
                    deleteBtn,
                    "Η διαγραφή ακυρώθηκε, δεν έγινε καμία αλλαγή",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            imageUris.add(photoUri!!)
            Timber.d("Added photo: $photoUri")


            buildingImageAdapter = BuildingImageAdapter(
                this,
                imageUris
            )
            buildingImages.adapter = buildingImageAdapter


        }

    }

    companion object {
        var photoUri: Uri? = null
    }

}