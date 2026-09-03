package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BirdAudioPlayer
import com.example.audio.PlaybackState
import com.example.classifier.BirdClassifier
import com.example.classifier.ClassificationResult
import com.example.data.local.BirdDatabase
import com.example.data.repository.BirdRepository
import com.example.data.repository.InitialBirdData
import com.example.location.LocationHelper
import com.example.location.UserLocation
import com.example.model.BirdSpecies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BirdFilter(val labelEs: String) {
    ALL("Todas"),
    THREATENED("En Riesgo"),
    WITH_AUDIO("Con Canto"),
    DISCOVERED("Descubiertas"),
    FAVORITES("Favoritas")
}

data class BirdRadarUiState(
    val birds: List<BirdSpecies> = InitialBirdData.defaultBirds,
    val filteredBirds: List<BirdSpecies> = InitialBirdData.defaultBirds,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val activeFilter: BirdFilter = BirdFilter.ALL,
    val currentLocation: UserLocation = UserLocation(-34.6037, -58.3816, "Reserva Natural Costanera Sur", true),
    val selectedBirdForDetail: BirdSpecies? = null,
    val isCameraOpen: Boolean = false,
    val isIdentifying: Boolean = false,
    val classificationResult: ClassificationResult? = null,
    val showCelebration: Boolean = false,
    val celebrationBird: BirdSpecies? = null,
    val errorMessage: String? = null
)

class BirdViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BirdDatabase.getInstance(application)
    private val repository = BirdRepository(
        birdDao = db.birdDao(),
        ebirdApiToken = null, // Will use BuildConfig if configured or rich live fallback
        iucnToken = null
    )
    val audioPlayer = BirdAudioPlayer(application)
    private val locationHelper = LocationHelper(application)
    private val classifier = BirdClassifier()

    private val _uiState = MutableStateFlow(BirdRadarUiState())
    val uiState: StateFlow<BirdRadarUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = audioPlayer.playbackState

    init {
        viewModelScope.launch {
            repository.initializeCacheIfNeeded()
            loadCachedBirdsAndObserve()
            detectGpsAndFetch()
        }
    }

    private fun loadCachedBirdsAndObserve() {
        viewModelScope.launch {
            repository.allBirdsFlow.collect { birds ->
                val currentList = if (birds.isEmpty()) InitialBirdData.defaultBirds else birds
                _uiState.value = _uiState.value.copy(
                    birds = currentList,
                    isLoading = false
                )
                applyCurrentFilters()
            }
        }
    }

    fun detectGpsAndFetch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            val location = locationHelper.getCurrentLocation()
            _uiState.value = _uiState.value.copy(currentLocation = location)

            repository.fetchNearbyBirds(location.latitude, location.longitude)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
            applyCurrentFilters()
        }
    }

    fun searchLocationOrCity(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            audioPlayer.playChirpFeedback()
            val location = locationHelper.getCoordinatesForQuery(query)
            _uiState.value = _uiState.value.copy(currentLocation = location)

            repository.fetchNearbyBirds(location.latitude, location.longitude)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
            applyCurrentFilters()
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyCurrentFilters()
    }

    fun setFilter(filter: BirdFilter) {
        audioPlayer.playChirpFeedback()
        _uiState.value = _uiState.value.copy(activeFilter = filter)
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        val state = _uiState.value
        var list = state.birds

        // 1. Text search
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase().trim()
            list = list.filter { bird ->
                bird.commonName.lowercase().contains(q) ||
                        bird.scientificName.lowercase().contains(q) ||
                        bird.familyName.lowercase().contains(q) ||
                        bird.description.lowercase().contains(q)
            }
        }

        // 2. Category filter
        list = when (state.activeFilter) {
            BirdFilter.ALL -> list
            BirdFilter.THREATENED -> list.filter { it.conservationStatus.isThreatened }
            BirdFilter.WITH_AUDIO -> list.filter { !it.audioUrl.isNullOrBlank() }
            BirdFilter.DISCOVERED -> list.filter { it.isDiscovered }
            BirdFilter.FAVORITES -> list.filter { it.isFavorite }
        }

        _uiState.value = state.copy(filteredBirds = list)
    }

    fun selectBirdForDetail(bird: BirdSpecies) {
        audioPlayer.playChirpFeedback()
        _uiState.value = _uiState.value.copy(selectedBirdForDetail = bird)
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedBirdForDetail = null)
    }

    fun toggleFavorite(bird: BirdSpecies) {
        audioPlayer.playChirpFeedback()
        val newFav = !bird.isFavorite
        viewModelScope.launch {
            repository.toggleFavorite(bird.scientificName, newFav)
            if (_uiState.value.selectedBirdForDetail?.scientificName == bird.scientificName) {
                _uiState.value = _uiState.value.copy(
                    selectedBirdForDetail = _uiState.value.selectedBirdForDetail?.copy(isFavorite = newFav)
                )
            }
        }
    }

    fun markDiscovered(bird: BirdSpecies) {
        viewModelScope.launch {
            repository.markDiscovered(bird.scientificName)
            _uiState.value = _uiState.value.copy(
                showCelebration = true,
                celebrationBird = bird
            )
        }
    }

    fun dismissCelebration() {
        _uiState.value = _uiState.value.copy(showCelebration = false, celebrationBird = null)
    }

    fun playBirdSong(bird: BirdSpecies) {
        audioPlayer.playOrPause(bird)
    }

    fun openCamera() {
        _uiState.value = _uiState.value.copy(isCameraOpen = true, classificationResult = null)
    }

    fun closeCamera() {
        _uiState.value = _uiState.value.copy(isCameraOpen = false, classificationResult = null)
    }

    fun classifyCapturedPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIdentifying = true)
            val result = classifier.classifyImage(bitmap, _uiState.value.birds)
            _uiState.value = _uiState.value.copy(
                isIdentifying = false,
                classificationResult = result
            )
            // Mark discovered automatically
            repository.markDiscovered(result.topMatch.scientificName)
        }
    }

    fun dismissClassification() {
        _uiState.value = _uiState.value.copy(classificationResult = null, isCameraOpen = false)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
