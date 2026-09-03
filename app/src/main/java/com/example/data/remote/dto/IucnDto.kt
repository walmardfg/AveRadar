package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IucnSpeciesResponse(
    @Json(name = "name") val name: String? = null,
    @Json(name = "result") val result: List<IucnSpeciesResult>? = null
)

@JsonClass(generateAdapter = true)
data class IucnSpeciesResult(
    @Json(name = "taxonid") val taxonId: Long? = null,
    @Json(name = "scientific_name") val scientificName: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "criteria") val criteria: String? = null,
    @Json(name = "population_trend") val populationTrend: String? = null,
    @Json(name = "marine_system") val marineSystem: Boolean? = null,
    @Json(name = "freshwater_system") val freshwaterSystem: Boolean? = null,
    @Json(name = "terrestrial_system") val terrestrialSystem: Boolean? = null
)
