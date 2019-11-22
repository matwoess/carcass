package com.mathias.android.carcass

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Carcass(
    var id: Long,
    type: AnimalType,
    description: String,
    reportedAt: Date,
    location: LatLng
) {
    var type: AnimalType? = type
    var description: String? = description
    var reportedat: Date? = reportedAt
    var location: LatLng? = location
}

enum class AnimalType {
    Hedgehog, Deer, Squirrel, Bird
}