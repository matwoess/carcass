package com.mathias.android.carcass

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import com.mathias.android.carcass.ActivityMaps.Companion.geocoder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class AddActivity : AppCompatActivity() {
    private lateinit var spnType: Spinner
    private lateinit var txtDescription: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtLocation: TextView
    private lateinit var btnTakePicture: Button
    private lateinit var btnDone: Button
    private lateinit var imageView: ImageView

    private lateinit var location: LatLng

    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val lat = intent.getDoubleExtra("location_lat", .0)
        val lng = intent.getDoubleExtra("location_lng", .0)
        Log.i(TAG, lat.toString())
        Log.i(TAG, lng.toString())
        location = LatLng(lat, lng)
        initUI()
        initButtons()
    }

    private fun initUI() {
        spnType = findViewById(R.id.spn_animal_type)
        txtDescription = findViewById(R.id.txt_animal_description)
        txtTime = findViewById(R.id.txt_current_time)
        txtLocation = findViewById(R.id.txt_current_location)
        btnTakePicture = findViewById(R.id.btn_take_picture)
        btnDone = findViewById(R.id.btn_done)
        val adapter =
            ArrayAdapter<CharSequence>(this, R.layout.support_simple_spinner_dropdown_item)
        AnimalType.values().forEach { t -> adapter.add(t.name) }
        adapter.add("Other...")
        spnType.adapter = adapter
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm");
        txtTime.text = dateFormat.format(Date())
        Log.i(TAG, geocoder.toString())
        val addresses: List<Address> = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )
        txtLocation.text = if (addresses.isNotEmpty()) addresses[0].thoroughfare else "N/A"
        imageView = findViewById(R.id.img_view_report)
    }


    private fun initButtons() {
        btnTakePicture.setOnClickListener { dispatchTakePictureIntent() }
        btnDone.setOnClickListener { finish() }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e(TAG, "error creating image file")
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.mathias.android.carcass",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "picture taken")
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) setPic()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setPic() {
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            val photoW: Int = outWidth
            val photoH: Int = outHeight
            val scaleFactor: Int = min(photoW / targetW, photoH / targetH)
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }

    companion object {
        private const val TAG = "AddActivity"
        private const val REQUEST_TAKE_PHOTO = 220
    }
}
