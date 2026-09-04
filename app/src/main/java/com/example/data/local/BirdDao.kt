package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds_cache ORDER BY isFavorite DESC, commonName ASC")
    fun getAllBirdsFlow(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds_cache WHERE commonName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' OR familyName LIKE '%' || :query || '%'")
    fun searchBirdsFlow(query: String): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds_cache WHERE scientificName = :scientificName LIMIT 1")
    suspend fun getBirdByScientificName(scientificName: String): BirdEntity?

    @Query("SELECT * FROM birds_cache WHERE isDiscovered = 1")
    fun getDiscoveredBirdsFlow(): Flow<List<BirdEntity>>

    @Query("SELECT * FROM birds_cache WHERE isFavorite = 1")
    fun getFavoriteBirdsFlow(): Flow<List<BirdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirds(birds: List<BirdEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBird(bird: BirdEntity)

    @Update
    suspend fun updateBird(bird: BirdEntity)

    @Query("UPDATE birds_cache SET isFavorite = :isFavorite WHERE scientificName = :scientificName")
    suspend fun updateFavorite(scientificName: String, isFavorite: Boolean)

    @Query("UPDATE birds_cache SET isDiscovered = :isDiscovered WHERE scientificName = :scientificName")
    suspend fun updateDiscovered(scientificName: String, isDiscovered: Boolean)

    @Query("SELECT COUNT(*) FROM birds_cache")
    suspend fun getBirdCount(): Int

    @Query("DELETE FROM birds_cache")
    suspend fun clearAll()

    @Query("DELETE FROM birds_cache WHERE isFavorite = 0")
    suspend fun clearNonFavorites()
}
