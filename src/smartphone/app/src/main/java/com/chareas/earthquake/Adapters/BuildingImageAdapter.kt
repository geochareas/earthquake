package com.chareas.earthquake.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.chareas.earthquake.Activities.AddAutopsy
import com.chareas.earthquake.Activities.ViewAutopsy
import com.chareas.earthquake.R
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BuildingImageAdapter(private val context: Context, images: MutableList<Uri>) :
    BaseAdapter() {
    private lateinit var currentPhotoPath: String
    private var origin: Activity
    private val REQUEST_IMAGE_CAPTURE = 1
    private var images: MutableList<Uri> = mutableListOf()


    // 1
    init {
        this.images = images
        origin = context as Activity
    }

    // 2
    override fun getCount(): Int {
        return images.size + 1
    }

    // 3
    override fun getItemId(position: Int): Long {
        return 0
    }

    // 4
    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView


        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.building_image, parent, false)
        }
        var imageView = convertView!!.findViewById<View>(R.id.building_image) as ImageView

        if (position == images.size) {
            imageView.setImageResource(R.drawable.ic_baseline_add_dark_24);
            imageView.setOnClickListener {

                dispatchTakePictureIntent()
            }
            return convertView
        }

        Picasso.get().load(images[position]).into(imageView);


        return convertView
    }





    private fun dispatchTakePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(origin.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        origin,
                        "com.chareas.earthquake.fileprovider",
                        it
                    )
                    ViewAutopsy.photoUri = photoURI // set current uri for AddBuilding activity

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    origin.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = origin.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }

}