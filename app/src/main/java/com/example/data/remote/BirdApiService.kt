package com.example.data.remote

import com.example.data.remote.dto.EbirdObservationDto
import com.example.data.remote.dto.INatSpeciesCountsResponse
import com.example.data.remote.dto.INatTaxaResponse
import com.example.data.remote.dto.IucnSpeciesResponse
import com.example.data.remote.dto.XenoCantoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EbirdApiService {
    @GET("v2/data/obs/geo/recent")
    suspend fun getRecentNearbyObservations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("dist") distKm: Int = 25,
        @Query("back") backDays: Int = 14,
        @Query("maxResults") maxResults: Int = 40,
        @Query("locale") locale: String = "es",
        @Header("X-eBirdApiToken") apiToken: String? = null
    ): List<EbirdObservationDto>

    @GET("v2/data/obs/{regionCode}/recent")
    suspend fun getRecentRegionObservations(
        @Path("regionCode") regionCode: String,
        @Query("back") backDays: Int = 14,
        @Query("maxResults") maxResults: Int = 40,
        @Query("locale") locale: String = "es",
        @Header("X-eBirdApiToken") apiToken: String? = null
    ): List<EbirdObservationDto>
}

interface INaturalistApiService {
    @GET("v1/taxa")
    suspend fun searchTaxa(
        @Query("q") query: String,
        @Query("taxon_id") taxonId: Int = 3, // Birds
        @Query("locale") locale: String = "es",
        @Query("per_page") perPage: Int = 5
    ): INatTaxaResponse

    @GET("v1/taxa/{id}")
    suspend fun getTaxonDetails(
        @Path("id") taxonId: Long,
        @Query("locale") locale: String = "es"
    ): INatTaxaResponse

    @GET("v1/observations/species_counts")
    suspend fun getNearbySpeciesCounts(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radiusKm: Int = 60,
        @Query("taxon_id") taxonId: Int = 3, // Birds
        @Query("locale") locale: String = "es",
        @Query("per_page") perPage: Int = 25
    ): INatSpeciesCountsResponse
}

interface XenoCantoApiService {
    @GET("api/2/recordings")
    suspend fun searchRecordings(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): XenoCantoResponse
}

interface IucnApiService {
    @GET("api/v3/species/{name}")
    suspend fun getSpeciesStatusV3(
        @Path("name") scientificName: String,
        @Query("token") apiToken: String? = null
    ): IucnSpeciesResponse

    @GET("api/v4/taxa/scientific_name")
    suspend fun getSpeciesStatusV4(
        @Query("scientific_name") scientificName: String,
        @Header("Authorization") authHeader: String? = null
    ): IucnSpeciesResponse
}
