package com.mathias.android.carcass

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Carcass {
    var id: Long = 0
    var type: AnimalType? = null
    var description: String? = null
    var reportedat: Date? = null
    var location: LatLng? = null

    constructor()
    constructor(
        id: Long,
        type: AnimalType,
        description: String,
        reportedAt: Date,
        location: LatLng
    ) {
        this.id = id
        this.type = type
        this.description = description
        this.reportedat = reportedAt
        this.location = location
    }
}


enum class AnimalType {
    Hedgehog, Deer, Squirrel, Bird
}