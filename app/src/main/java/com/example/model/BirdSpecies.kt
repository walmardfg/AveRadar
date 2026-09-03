package com.example.model

data class BirdSpecies(
    val scientificName: String,
    val commonName: String,
    val familyName: String = "",
    val description: String,
    val conservationStatus: ConservationStatus,
    val audioUrl: String? = null,
    val photoUrls: List<String> = emptyList(),
    val soundDuration: String = "0:15",
    val funFact: String = "",
    val wingspan: String = "30-40 cm",
    val diet: String = "Semillas e insectos",
    val distanceKm: Double? = null,
    val isFavorite: Boolean = false,
    val isDiscovered: Boolean = false
) {
    val primaryPhotoUrl: String
        get() = photoUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=800&auto=format&fit=crop&q=60"
}
