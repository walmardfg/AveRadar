package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class XenoCantoResponse(
    @Json(name = "numRecordings") val numRecordings: String? = null,
    @Json(name = "numSpecies") val numSpecies: String? = null,
    @Json(name = "page") val page: Int? = null,
    @Json(name = "numPages") val numPages: Int? = null,
    @Json(name = "recordings") val recordings: List<XenoCantoRecording>? = null
)

@JsonClass(generateAdapter = true)
data class XenoCantoRecording(
    @Json(name = "id") val id: String? = null,
    @Json(name = "gen") val gen: String? = null,
    @Json(name = "sp") val sp: String? = null,
    @Json(name = "ssp") val ssp: String? = null,
    @Json(name = "en") val en: String? = null,
    @Json(name = "rec") val rec: String? = null,
    @Json(name = "cnt") val cnt: String? = null,
    @Json(name = "loc") val loc: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "file") val file: String? = null,
    @Json(name = "file-name") val fileName: String? = null,
    @Json(name = "length") val length: String? = null,
    @Json(name = "q") val quality: String? = null
)
