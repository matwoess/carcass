package com.mathias.android.carcass.model


class AnimalType(
    var name: String
) {
    constructor() : this("")

    override fun toString(): String {
        return name;
    }
}