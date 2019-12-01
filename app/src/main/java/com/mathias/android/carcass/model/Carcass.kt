package com.mathias.android.carcass.model

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

class Carcass {
    var id: Long? = null
    var type: AnimalType? = null
    var description: String? = null
    var reportedAt: Date? = null
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
        this.reportedAt = reportedAt
        this.location = location

    }

    fun toCarcassDB(): CarcassDB {
        var type =
            AnimalType(this.type.toString())
        return CarcassDB(
            this.id!!.toString(),
            type.toString(),
            this.description!!,
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(this.reportedAt!!),
            LocationDB(
                this.location!!.latitude.toString(),
                this.location!!.longitude.toString()
            )
        )
    }

    override fun toString(): String {
        return StringBuilder()
            .append("[")
            .append(id)
            .append(", ")
            .append(type)
            .append(", ")
            .append(description)
            .append(", ")
            .append(reportedAt)
            .append(", ")
            .append(location)
            .append("]")
            .toString()
    }
}
