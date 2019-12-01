package com.mathias.android.carcass.model

class LocationDB {
    var lat: String? = null
    var lng: String? = null
    constructor() {}
    constructor(lat: String, lng:String) {
        this.lat = lat
        this.lng = lng
    }
}