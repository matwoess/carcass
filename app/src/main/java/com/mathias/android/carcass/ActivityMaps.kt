package com.mathias.android.carcass

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_BUNDLE
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_DESCRIPTION
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_LOCATION_LAT
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_LOCATION_LNG
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_TIME
import com.mathias.android.carcass.AddActivity.Companion.CARCASS_TYPE
import com.mathias.android.carcass.AddActivity.Companion.EXISTING_KEY
import com.mathias.android.carcass.FireDBHelper.Companion.animalTypes
import com.mathias.android.carcass.FireDBHelper.Companion.markers
import com.mathias.android.carcass.model.AnimalType
import com.mathias.android.carcass.model.Carcass
import java.util.*

class ActivityMaps : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates: Boolean = true
    private lateinit var mFab: FloatingActionButton
    private var lastLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(applicationContext, Locale.getDefault())
        mFab = findViewById(R.id.floatingActionButton)
        mFab.setOnClickListener { handleFabClick() }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 5 * 1000
        locationRequest.fastestInterval = 1 * 1000
        locationRequest.maxWaitTime = 10 * 1000
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                updateLocation(locationResult)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "map ready")
        mMap = googleMap
        fireDBHandler = FireDBHelper(mMap)
        fireDBHandler.initFirebaseDB()
        mMap.uiSettings.isZoomControlsEnabled = true
        initLocation()
        mMap.setOnMarkerClickListener { latLng -> handleMarkerClick(mMap, latLng) }
    }

    private fun updateLocation(locationResult: LocationResult?) {
        Log.i(TAG, "updateLocation")
        Toast.makeText(applicationContext, "location received", Toast.LENGTH_SHORT).show()
        locationResult ?: return
        Log.i(TAG, "got %d location(s)".format(locationResult.locations.size))
        if (locationResult.lastLocation != null) {
            lastLocation = LatLng(
                locationResult.lastLocation.latitude,
                locationResult.lastLocation.longitude
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 12f))
        }
    }

    private fun handleFabClick() {
        val intent = Intent(this, AddActivity::class.java).apply {
            putExtra(CARCASS_LOCATION_LAT, lastLocation?.latitude)
            putExtra(CARCASS_LOCATION_LNG, lastLocation?.longitude)
        }
        startActivityForResult(intent, ADD_REQUEST_CODE)
    }

    private fun handleMarkerClick(mMap: GoogleMap, marker: Marker): Boolean {
        showBottomSheet(markers[marker]!!)
        return true
    }

    private fun initLocation() {
        Log.i(TAG, "init location")
        if (!checkPermissions()) return
        mMap.isMyLocationEnabled = true
        startLocationUpdates()
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "ask for permissions")
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERM_LOCATION
            )
            return false
        }
        Log.i(TAG, "permissions exist")
        return true
    }

    private fun showBottomSheet(key: String) {
        val sheet = BottomSheetInfo().newInstance(key)
        sheet.show(this.supportFragmentManager, "Carcass Info")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.demo_data -> {
                if (animalTypes.isNotEmpty()) fireDBHandler.insertDemoData(lastLocation!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")
        if (requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "result is OK")
            if (data == null) return
            val bundle = data.getBundleExtra(CARCASS_BUNDLE) ?: return
            Log.i(TAG, "get carcass data")
            val new = getCarcassFromBundle(bundle)
            Log.i(TAG, "create new entry in DB")
            fireDBHandler.pushCarcass(new)
        } else if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "result is OK")
            if (data == null) return
            val bundle = data.getBundleExtra(CARCASS_BUNDLE) ?: return
            Log.i(TAG, "get carcass data")
            val updated = getCarcassFromBundle(bundle)
            Log.i(TAG, "update existing entry in DB")
            val key = bundle.getString(EXISTING_KEY)
            Log.i(TAG, "key = $key")
            fireDBHandler.updateCarcass(key!!, updated)
        }
    }

    private fun getCarcassFromBundle(bundle: Bundle): Carcass {
        val type = bundle.getString(CARCASS_TYPE)
        val description = bundle.getString(CARCASS_DESCRIPTION)
        val time = bundle.getLong(CARCASS_TIME)
        val lat = bundle.getDouble(CARCASS_LOCATION_LAT)
        val lng = bundle.getDouble(CARCASS_LOCATION_LNG)
        return Carcass(AnimalType(type!!), description, time, LatLng(lat, lng))
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdates = false
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        requestingLocationUpdates = true
    }

    companion object {
        private const val TAG = "ActivityMaps";

        lateinit var geocoder: Geocoder
        lateinit var fireDBHandler: FireDBHelper

        private const val REQUEST_PERM_LOCATION = 100
        private const val ADD_REQUEST_CODE = 200
        const val EDIT_REQUEST_CODE = 210
    }
}
