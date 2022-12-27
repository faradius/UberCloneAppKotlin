package com.alex.ubercloneapp.models

// To parse the JSON, install Klaxon and do:
//
//   val prices = Prices.fromJson(jsonString)

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Prices (
    val km: Double? = null,
    val min: Double? = null,
    val minValue: Double? = null,
    val difference: Double? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Prices>(json)
    }
}
