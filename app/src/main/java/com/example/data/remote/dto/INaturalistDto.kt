package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class INatTaxaResponse(
    @Json(name = "total_results") val totalResults: Int? = null,
    @Json(name = "results") val results: List<INatTaxonResult>? = null
)

@JsonClass(generateAdapter = true)
data class INatSpeciesCountsResponse(
    @Json(name = "total_results") val totalResults: Int? = null,
    @Json(name = "results") val results: List<INatSpeciesCountResult>? = null
)

@JsonClass(generateAdapter = true)
data class INatSpeciesCountResult(
    @Json(name = "count") val count: Int? = null,
    @Json(name = "taxon") val taxon: INatTaxonResult? = null
)

@JsonClass(generateAdapter = true)
data class INatTaxonResult(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "preferred_common_name") val preferredCommonName: String? = null,
    @Json(name = "rank") val rank: String? = null,
    @Json(name = "wikipedia_summary") val wikipediaSummary: String? = null,
    @Json(name = "default_photo") val defaultPhoto: INatPhoto? = null,
    @Json(name = "taxon_photos") val taxonPhotos: List<INatTaxonPhoto>? = null,
    @Json(name = "conservation_status") val conservationStatus: INatConservationStatus? = null
)

@JsonClass(generateAdapter = true)
data class INatPhoto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "medium_url") val mediumUrl: String? = null,
    @Json(name = "large_url") val largeUrl: String? = null,
    @Json(name = "original_url") val originalUrl: String? = null,
    @Json(name = "square_url") val squareUrl: String? = null,
    @Json(name = "attribution") val attribution: String? = null
)

@JsonClass(generateAdapter = true)
data class INatTaxonPhoto(
    @Json(name = "photo") val photo: INatPhoto? = null
)

@JsonClass(generateAdapter = true)
data class INatConservationStatus(
    @Json(name = "status") val status: String? = null,
    @Json(name = "status_name") val statusName: String? = null,
    @Json(name = "iucn") val iucn: Int? = null
)

@JsonClass(generateAdapter = true)
data class INatObservationsResponse(
    @Json(name = "total_results") val totalResults: Int? = null,
    @Json(name = "results") val results: List<INatObservationItem>? = null
)

@JsonClass(generateAdapter = true)
data class INatObservationItem(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "sounds") val sounds: List<INatSoundItem>? = null
)

@JsonClass(generateAdapter = true)
data class INatSoundItem(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "file_url") val fileUrl: String? = null
)
