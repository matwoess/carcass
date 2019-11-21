package com.mathias.android.carcass

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class ActivityMaps : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMapClickListener { latLng -> handleMapClick(mMap, latLng) }
        updateLocation();
    }

    private fun handleMapClick(mMap: GoogleMap, latLng: LatLng?) {
        showBottomSheet(Carcass())

    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val REQUEST_PERM_LOCATION = 100
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERM_LOCATION)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.isMyLocationEnabled = true
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    insertDemoData(currentLatLng)
                }
            }
        }
    }

    private fun showBottomSheet(carcass: Carcass) {
        val sheet = BottomSheetInfo().newInstance(carcass)
        sheet.show(this.supportFragmentManager, "Bottom Sheet Dialog")
    }

    private fun insertDemoData(currentLatLng: LatLng) {
        // Add a marker in Sydney and move the camera
        val rand: Random = Random();
        val scale = 1/80.0;
        for (i in 0..1) {
            val carcass1 = LatLng(currentLatLng.latitude+rand.nextDouble()*scale, currentLatLng.longitude+rand.nextDouble()*scale)
            val carcass2 = LatLng(currentLatLng.latitude+rand.nextDouble()*scale, currentLatLng.longitude-rand.nextDouble()*scale)
            val carcass3 = LatLng(currentLatLng.latitude-rand.nextDouble()*scale, currentLatLng.longitude-rand.nextDouble()*scale)
            val carcass4 = LatLng(currentLatLng.latitude-rand.nextDouble()*scale, currentLatLng.longitude+rand.nextDouble()*scale)
            mMap.addMarker(MarkerOptions().position(carcass1).title("Carcass"))
            mMap.addMarker(MarkerOptions().position(carcass2).title("Carcass"))
            mMap.addMarker(MarkerOptions().position(carcass3).title("Carcass"))
            mMap.addMarker(MarkerOptions().position(carcass4).title("Carcass"))
        }
    }
}
