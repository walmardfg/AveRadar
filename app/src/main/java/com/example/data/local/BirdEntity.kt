package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BirdSpecies
import com.example.model.ConservationStatus

@Entity(tableName = "birds_cache")
data class BirdEntity(
    @PrimaryKey
    val scientificName: String,
    val commonName: String,
    val familyName: String,
    val description: String,
    val conservationCode: String,
    val audioUrl: String?,
    val photoUrls: List<String>,
    val soundDuration: String,
    val funFact: String,
    val wingspan: String,
    val diet: String,
    val distanceKm: Double?,
    val isFavorite: Boolean,
    val isDiscovered: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): BirdSpecies {
        return BirdSpecies(
            scientificName = scientificName,
            commonName = commonName,
            familyName = familyName,
            description = description,
            conservationStatus = ConservationStatus.fromCode(conservationCode),
            audioUrl = audioUrl,
            photoUrls = photoUrls,
            soundDuration = soundDuration,
            funFact = funFact,
            wingspan = wingspan,
            diet = diet,
            distanceKm = distanceKm,
            isFavorite = isFavorite,
            isDiscovered = isDiscovered
        )
    }

    companion object {
        fun fromDomain(bird: BirdSpecies): BirdEntity {
            return BirdEntity(
                scientificName = bird.scientificName,
                commonName = bird.commonName,
                familyName = bird.familyName,
                description = bird.description,
                conservationCode = bird.conservationStatus.code,
                audioUrl = bird.audioUrl,
                photoUrls = bird.photoUrls,
                soundDuration = bird.soundDuration,
                funFact = bird.funFact,
                wingspan = bird.wingspan,
                diet = bird.diet,
                distanceKm = bird.distanceKm,
                isFavorite = bird.isFavorite,
                isDiscovered = bird.isDiscovered
            )
        }
    }
}
