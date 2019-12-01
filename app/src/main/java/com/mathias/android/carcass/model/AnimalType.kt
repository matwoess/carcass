package com.mathias.android.carcass.model


class AnimalType (
    val name: String
) {
    constructor() : this("")

    override fun toString(): String {
        return name;
    }
}