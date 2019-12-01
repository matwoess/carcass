package com.mathias.android.carcass.model

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat

class CarcassDB {
    var id: String? = null
    var type: String? = null
    var description: String? = null
    var reportedAt: String? = null
    var location: LocationDB? = null

    constructor() {}
    constructor(
        id: String,
        type: String,
        description: String,
        reportedAt: String,
        location: LocationDB
    ) {
        this.id = id
        this.type = type
        this.description = description
        this.reportedAt = reportedAt
        this.location = location

    }

    fun toCarcass(): Carcass {
        var type = AnimalType(this.type!!)
        return Carcass(
            this.id!!,
            type,
            this.description!!,
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(this.reportedAt),
            LatLng(this.location!!.lat!!.toDouble(), this.location!!.lng!!.toDouble())
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
