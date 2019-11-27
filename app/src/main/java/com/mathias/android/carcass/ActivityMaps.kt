package com.mathias.android.carcass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFab: FloatingActionButton
    private var carcasses: HashMap<Marker, Carcass> = HashMap()
    private var lastLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mFab = findViewById(R.id.floatingActionButton)
        mFab.setOnClickListener { handleFabClick() }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                Log.i(TAG, "got %d locations".format(locationResult.locations.size))
                if (locationResult.lastLocation != null) {
                    lastLocation = LatLng(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 12f))
                }
            }
        }
    }

    private fun handleFabClick() {
        val intent = Intent(this, AddActivity::class.java).apply {
            putExtra("location_lat", lastLocation?.latitude)
            putExtra("location_lng", lastLocation?.longitude)
        }
        startActivityForResult(intent, ADD_REQUEST_CODE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateLocation()
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.setOnMarkerClickListener { latLng -> handleMarkerClick(mMap, latLng) }
        val locationRequest = LocationRequest()
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun handleMarkerClick(mMap: GoogleMap, marker: Marker): Boolean {
        showBottomSheet(carcasses[marker]!!)
        return true
    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERM_LOCATION
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    Log.i(TAG, "got last known location")
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    insertDemoData(currentLatLng)
                    lastLocation = currentLatLng
                }
            }
        }
    }

    private fun showBottomSheet(carcass: Carcass) {
        val sheet = BottomSheetInfo().newInstance(carcass)
        sheet.show(this.supportFragmentManager, "Carcass Info")
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
        private const val TAG = "ActivityMaps";
        private const val REQUEST_PERM_LOCATION = 100
        private const val ADD_REQUEST_CODE = 200
    }
}
