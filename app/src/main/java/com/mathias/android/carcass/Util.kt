package com.mathias.android.carcass

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

fun insertDemoData(userPos: LatLng, mMap: GoogleMap, carcasses: HashMap<Marker, Carcass>) {
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