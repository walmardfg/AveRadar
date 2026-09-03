package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EbirdObservationDto(
    @Json(name = "speciesCode") val speciesCode: String? = null,
    @Json(name = "comName") val comName: String? = null,
    @Json(name = "sciName") val sciName: String? = null,
    @Json(name = "locId") val locId: String? = null,
    @Json(name = "locName") val locName: String? = null,
    @Json(name = "obsDt") val obsDt: String? = null,
    @Json(name = "howMany") val howMany: Int? = null,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lng") val lng: Double? = null,
    @Json(name = "obsValid") val obsValid: Boolean? = null,
    @Json(name = "obsReviewed") val obsReviewed: Boolean? = null,
    @Json(name = "locationPrivate") val locationPrivate: Boolean? = null
)
