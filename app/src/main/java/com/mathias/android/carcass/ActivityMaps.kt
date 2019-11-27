package com.mathias.android.carcass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.collections.HashMap

class ActivityMaps : AppCompatActivity(), OnMapReadyCallback {
    private var requestingLocationUpdates: Boolean = true
    private lateinit var locationRequest: LocationRequest
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFab: FloatingActionButton
    private var carcasses: HashMap<Marker, Carcass> = HashMap()
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
            if (carcasses.isEmpty()) insertDemoData(lastLocation!!)
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
        showBottomSheet(carcasses[marker]!!)
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
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
        requestingLocationUpdates = true
    }

    private fun insertDemoData(userPos: LatLng) {
        val rand = Random()
        val scale = 1 / 80.0
        for (i in 0..1) {
            val carcass1 = LatLng(
                userPos.latitude + rand.nextDouble() * scale,
                userPos.longitude + rand.nextDouble() * scale
            )
            val carcass2 = LatLng(
                userPos.latitude + rand.nextDouble() * scale,
                userPos.longitude - rand.nextDouble() * scale
            )
            val carcass3 = LatLng(
                userPos.latitude - rand.nextDouble() * scale,
                userPos.longitude - rand.nextDouble() * scale
            )
            val carcass4 = LatLng(
                userPos.latitude - rand.nextDouble() * scale,
                userPos.longitude + rand.nextDouble() * scale
            )
            carcasses[mMap.addMarker(MarkerOptions().position(carcass1).title("Hedgehog"))] =
                Carcass((i + 1) * 1L, AnimalType.Hedgehog, "a dead hedgehog", Date(), carcass1)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass2).title("Deer"))] =
                Carcass((i + 1) * 2L, AnimalType.Deer, "a dead deer", Date(), carcass2)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass3).title("Squirrel"))] =
                Carcass((i + 1) * 3L, AnimalType.Squirrel, "a dead squirrel", Date(), carcass3)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass4).title("Bird"))] =
                Carcass((i + 1) * 4L, AnimalType.Bird, "a dead bird", Date(), carcass4)
        }
    }

    companion object {
        lateinit var geocoder: Geocoder
        private const val TAG = "ActivityMaps";
        private const val REQUEST_PERM_LOCATION = 100
        private const val ADD_REQUEST_CODE = 200
    }
}
