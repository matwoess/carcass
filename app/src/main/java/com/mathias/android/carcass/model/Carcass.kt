package com.mathias.android.carcass.model

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*

class Carcass {
    var type: AnimalType? = null
    var description: String? = null
    var reportedAt: Date? = null
    var location: LatLng? = null

    constructor()
    constructor(
        type: AnimalType?,
        description: String?,
        reportedAt: Date?,
        location: LatLng
    ) {
        this.type = type
        this.description = description
        this.reportedAt = reportedAt
        this.location = location
    }

    fun toCarcassDB(): CarcassDB {
        var type =
            AnimalType(this.type.toString())
        return CarcassDB(
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

    fun updateValues(c: Carcass) {
        this.type = c.type
        this.location = c.location
        this.description = c.description
        this.reportedAt =  c.reportedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Carcass

        if (type != other.type) return false
        if (description != other.description) return false
        if (reportedAt != other.reportedAt) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (reportedAt?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}
