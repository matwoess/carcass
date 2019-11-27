package com.mathias.android.carcass

import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.mathias.android.carcass.ActivityMaps.Companion.geocoder
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {
    private lateinit var location: LatLng

    private lateinit var spnType: Spinner
    private lateinit var txtDescription: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtLocation: TextView
    private lateinit var btnTakePicture: Button
    private lateinit var btnDone: Button

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

    private fun initButtons() {
        btnTakePicture.setOnClickListener {
            Snackbar.make(
                findViewById(R.id.lyt_add_activity),
                "Take picture",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnDone.setOnClickListener { finish() }
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
    }

    companion object {
        private const val TAG = "AddActivity"
    }
}
