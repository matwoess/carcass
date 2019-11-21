package com.mathias.android.carcass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
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
import java.util.*
import kotlin.collections.HashMap

class ActivityMaps : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var carcasses: HashMap<Marker, Carcass> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener { latLng -> handleMarkerClick(mMap, latLng) }
        updateLocation();
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
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.isMyLocationEnabled = true
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                    insertDemoData(currentLatLng)
                    val locationRequest = LocationRequest()
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
        }
    }

    private fun showBottomSheet(carcass: Carcass) {
        val sheet = BottomSheetInfo().newInstance(carcass)
        sheet.show(this.supportFragmentManager, "Carcass Details")
    }

    private fun insertDemoData(currentLatLng: LatLng) {
        val rand = Random();
        val scale = 1 / 80.0;
        for (i in 0..1) {
            val carcass1 = LatLng(
                currentLatLng.latitude + rand.nextDouble() * scale,
                currentLatLng.longitude + rand.nextDouble() * scale
            )
            val carcass2 = LatLng(
                currentLatLng.latitude + rand.nextDouble() * scale,
                currentLatLng.longitude - rand.nextDouble() * scale
            )
            val carcass3 = LatLng(
                currentLatLng.latitude - rand.nextDouble() * scale,
                currentLatLng.longitude - rand.nextDouble() * scale
            )
            val carcass4 = LatLng(
                currentLatLng.latitude - rand.nextDouble() * scale,
                currentLatLng.longitude + rand.nextDouble() * scale
            )
            carcasses[mMap.addMarker(MarkerOptions().position(carcass1).title("Carcass"))] =
                Carcass((i + 1) * 1L, AnimalType.Hedgehog, "Dead Hedgehog", Date(), carcass1)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass2).title("Carcass"))] =
                Carcass((i + 1) * 2L, AnimalType.Deer, "Dead Deer", Date(), carcass2)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass3).title("Carcass"))] =
                Carcass((i + 1) * 3L, AnimalType.Squirrel, "Dead Squirrel", Date(), carcass3)
            carcasses[mMap.addMarker(MarkerOptions().position(carcass4).title("Carcass"))] =
                Carcass((i + 1) * 4L, AnimalType.Bird, "Dead Bird", Date(), carcass4)
        }
    }

    companion object {
        private const val REQUEST_PERM_LOCATION = 100
    }
}
