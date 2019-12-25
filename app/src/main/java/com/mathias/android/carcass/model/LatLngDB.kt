package com.mathias.android.carcass.model

import com.google.android.gms.maps.model.LatLng

class LatLngDB {
    var lat: Double = .0
    var lng: Double = .0

    constructor() {}
    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }
}