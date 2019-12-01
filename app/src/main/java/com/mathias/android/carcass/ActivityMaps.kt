package com.mathias.android.carcass

import android.Manifest
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
import com.mathias.android.carcass.FireDBHelper.Companion.animalTypes
import com.mathias.android.carcass.FireDBHelper.Companion.markers
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

    private val fireDBHandler: FireDBHelper = FireDBHelper()

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
        fireDBHandler.initFirebaseDB(mMap)
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
            putExtra("location_lat", lastLocation?.latitude)
            putExtra("location_lng", lastLocation?.longitude)
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

    private fun showBottomSheet(carcass: Carcass) {
        val sheet = BottomSheetInfo().newInstance(carcass)
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
                if (animalTypes.isNotEmpty()) fireDBHandler.insertDemoData(lastLocation!!, mMap)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        lateinit var geocoder: Geocoder
        private const val TAG = "ActivityMaps";
        private const val REQUEST_PERM_LOCATION = 100
        private const val ADD_REQUEST_CODE = 200
    }
}
