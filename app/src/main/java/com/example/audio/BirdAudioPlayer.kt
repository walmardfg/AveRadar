package com.example.audio

import android.content.Context
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.remote.NetworkClient
import com.example.model.BirdSpecies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

data class PlaybackState(
    val currentBirdId: String? = null,
    val audioUrl: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)

@OptIn(UnstableApi::class)
class BirdAudioPlayer(private val context: Context) {

    private val player: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var audioResolutionJob: Job? = null
    private var activeAudioTrack: android.media.AudioTrack? = null
    private var isPlayingSyntheticTrack = false

    private var currentPlayingBird: BirdSpecies? = null
    private var hasAttemptedSyntheticFallback = false

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    _playbackState.value = _playbackState.value.copy(
                        isBuffering = true,
                        error = null
                    )
                }
                Player.STATE_READY -> {
                    val duration = player.duration.coerceAtLeast(0L)
                    _playbackState.value = _playbackState.value.copy(
                        isBuffering = false,
                        isPlaying = player.isPlaying,
                        durationMs = duration,
                        error = null
                    )
                    startProgressTracking()
                }
                Player.STATE_ENDED -> {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        progress = 0f,
                        currentPositionMs = 0L
                    )
                    stopProgressTracking()
                }
                Player.STATE_IDLE -> {
                    if (!isPlayingSyntheticTrack) {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                        stopProgressTracking()
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startProgressTracking()
            } else if (!isPlayingSyntheticTrack) {
                stopProgressTracking()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.w("BirdAudioPlayer", "ExoPlayer playback error: ${error.message}")
            if (!hasAttemptedSyntheticFallback) {
                hasAttemptedSyntheticFallback = true
                currentPlayingBird?.let { bird ->
                    playSyntheticFallback(bird)
                } ?: run {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        error = "Error al reproducir audio"
                    )
                    stopProgressTracking()
                }
            } else {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    error = "Error al reproducir audio"
                )
                stopProgressTracking()
            }
        }
    }

    private fun getCacheFileForBird(scientificName: String): File {
        val sanitized = scientificName.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        return File(context.cacheDir, "audio_$sanitized.wav")
    }

    private fun getMp3CacheFileForBird(scientificName: String): File {
        val sanitized = scientificName.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        return File(context.cacheDir, "audio_$sanitized.mp3")
    }

    fun playOrPause(bird: BirdSpecies) {
        val current = _playbackState.value
        if (current.currentBirdId == bird.scientificName) {
            if (current.isPlaying) {
                stop()
                return
            } else if (player.playbackState == Player.STATE_READY) {
                player.play()
                _playbackState.value = current.copy(isPlaying = true)
                return
            }
        }

        stop()
        hasAttemptedSyntheticFallback = false
        currentPlayingBird = bird

        // 1. Check if we already have a cached audio file (WAV or MP3)
        val wavCache = getCacheFileForBird(bird.scientificName)
        val mp3Cache = getMp3CacheFileForBird(bird.scientificName)

        if (wavCache.exists() && wavCache.length() > 2000) {
            playLocalAudioFile(bird, wavCache)
            return
        }
        if (mp3Cache.exists() && mp3Cache.length() > 2000) {
            playLocalAudioFile(bird, mp3Cache)
            return
        }

        // 2. Set buffering state and resolve audio source
        _playbackState.value = PlaybackState(
            currentBirdId = bird.scientificName,
            audioUrl = bird.audioUrl,
            isPlaying = true,
            isBuffering = true
        )

        audioResolutionJob?.cancel()
        audioResolutionJob = scope.launch {
            resolveAndPlayAudio(bird, wavCache, mp3Cache)
        }
    }

    private suspend fun resolveAndPlayAudio(bird: BirdSpecies, wavDest: File, mp3Dest: File) {
        // A. Resolve candidate URL from catalog or bird object
        var streamUrl: String? = null

        if (!bird.audioUrl.isNullOrBlank()) {
            streamUrl = bird.audioUrl
        } else {
            val catalogUrl = BirdAudioCatalog.getAudioUrl(bird.scientificName, bird.commonName)
            if (!catalogUrl.isNullOrBlank()) {
                streamUrl = catalogUrl
            }
        }

        // B. If not in catalog, search iNaturalist sounds API
        if (streamUrl.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val inatResponse = NetworkClient.iNaturalistApi.getObservationsWithSound(
                        taxonName = bird.scientificName
                    )
                    streamUrl = inatResponse.results?.firstOrNull()?.sounds?.firstOrNull()?.fileUrl
                } catch (e: Exception) {
                    Log.w("BirdAudioPlayer", "iNat sound search error: ${e.message}")
                }
            }
        }

        // C. If streamable URL exists, stream directly via ExoPlayer!
        if (!streamUrl.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                playDirectStream(bird, streamUrl!!)
            }

            // Cache in background for offline use without blocking playback
            scope.launch(Dispatchers.IO) {
                try {
                    val isWav = streamUrl!!.contains(".wav", ignoreCase = true)
                    val targetFile = if (isWav) wavDest else mp3Dest
                    val request = Request.Builder()
                        .url(streamUrl!!)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                        .build()
                    val response = NetworkClient.okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            val tempFile = File(context.cacheDir, "temp_cache_${System.currentTimeMillis()}.${if (isWav) "wav" else "mp3"}")
                            FileOutputStream(tempFile).use { fos ->
                                input.copyTo(fos)
                            }
                            if (tempFile.length() > 2000) {
                                tempFile.renameTo(targetFile)
                            } else {
                                tempFile.delete()
                            }
                        }
                    }
                    response.close()
                } catch (_: Exception) {}
            }
            return
        }

        // D. If no online sound is found, immediately play synthesized avian song
        playSyntheticFallback(bird)
    }

    private fun playDirectStream(bird: BirdSpecies, url: String) {
        try {
            stopSyntheticTrack()
            player.clearMediaItems()
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _playbackState.value = PlaybackState(
                currentBirdId = bird.scientificName,
                audioUrl = url,
                isPlaying = true,
                isBuffering = true
            )
        } catch (e: Exception) {
            Log.e("BirdAudioPlayer", "Failed playing direct stream", e)
            playSyntheticFallback(bird)
        }
    }

    private fun playLocalAudioFile(bird: BirdSpecies, file: File) {
        try {
            stopSyntheticTrack()
            player.clearMediaItems()
            val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _playbackState.value = PlaybackState(
                currentBirdId = bird.scientificName,
                audioUrl = bird.audioUrl,
                isPlaying = true,
                isBuffering = false
            )
        } catch (e: Exception) {
            Log.e("BirdAudioPlayer", "Failed playing local file", e)
            playSyntheticFallback(bird)
        }
    }

    private fun playSyntheticFallback(bird: BirdSpecies) {
        scope.launch(Dispatchers.Main) {
            try {
                stopSyntheticTrack()
                try {
                    player.stop()
                } catch (_: Exception) {}

                val wavCache = getCacheFileForBird(bird.scientificName)
                val track = withContext(Dispatchers.Default) {
                    BirdSongSynthesizer.generateSongWav(bird, wavCache)
                    BirdSongSynthesizer.playSongWithAudioTrack(bird)
                }

                if (track != null) {
                    activeAudioTrack = track
                    isPlayingSyntheticTrack = true
                    _playbackState.value = PlaybackState(
                        currentBirdId = bird.scientificName,
                        isPlaying = true,
                        isBuffering = false,
                        durationMs = 3000L
                    )

                    // Track synthetic progress
                    scope.launch {
                        val startTime = System.currentTimeMillis()
                        val duration = 3000L
                        while (isActive && isPlayingSyntheticTrack) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val prog = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                            _playbackState.value = _playbackState.value.copy(
                                currentPositionMs = elapsed,
                                progress = prog,
                                isPlaying = true
                            )
                            if (elapsed >= duration) {
                                isPlayingSyntheticTrack = false
                                _playbackState.value = _playbackState.value.copy(
                                    isPlaying = false,
                                    progress = 0f,
                                    currentPositionMs = 0L
                                )
                                stopSyntheticTrack()
                                break
                            }
                            delay(100)
                        }
                    }
                } else {
                    _playbackState.value = PlaybackState(
                        currentBirdId = bird.scientificName,
                        isPlaying = false,
                        isBuffering = false,
                        error = "No se pudo reproducir el canto"
                    )
                }
            } catch (e: Exception) {
                Log.e("BirdAudioPlayer", "Synthetic fallback error", e)
                _playbackState.value = PlaybackState(
                    currentBirdId = bird.scientificName,
                    isPlaying = false,
                    isBuffering = false,
                    error = "Error al reproducir audio"
                )
            }
        }
    }

    private fun stopSyntheticTrack() {
        isPlayingSyntheticTrack = false
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null
    }

    fun playOrPause(birdId: String, audioUrl: String?) {
        val current = _playbackState.value
        if (current.currentBirdId == birdId && current.isPlaying) {
            player.pause()
            _playbackState.value = current.copy(isPlaying = false)
            return
        }

        val bird = currentPlayingBird?.takeIf { it.scientificName == birdId }
            ?: BirdSpecies(
                scientificName = birdId,
                commonName = birdId,
                familyName = "",
                description = "",
                conservationStatus = com.example.model.ConservationStatus.LEAST_CONCERN,
                audioUrl = audioUrl
            )
        playOrPause(bird)
    }

    fun seekTo(progressFraction: Float) {
        val duration = player.duration
        if (duration > 0) {
            val targetMs = (progressFraction * duration).toLong()
            player.seekTo(targetMs)
        }
    }

    fun stop() {
        audioResolutionJob?.cancel()
        audioResolutionJob = null
        stopSyntheticTrack()
        try {
            player.stop()
        } catch (_: Exception) {}
        stopProgressTracking()
        _playbackState.value = PlaybackState()
    }

    fun release() {
        audioResolutionJob?.cancel()
        stopSyntheticTrack()
        stopProgressTracking()
        try {
            player.release()
        } catch (_: Exception) {}
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && player.isPlaying) {
                val current = player.currentPosition.coerceAtLeast(0L)
                val duration = player.duration.coerceAtLeast(1L)
                val prog = (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = current,
                    durationMs = duration,
                    progress = prog,
                    isPlaying = true,
                    isBuffering = false
                )
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    /**
     * Real-time gentle chirp click interaction feedback
     */
    fun playChirpFeedback() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationMs = 100
                val numSamples = (durationMs * sampleRate) / 1000
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 2400.0 + 1000.0 * sin(Math.PI * t / (durationMs / 1000.0))
                    val envelope = sin(Math.PI * i / numSamples)
                    val sample = (sin(2.0 * Math.PI * freq * t) * envelope * Short.MAX_VALUE * 0.3).toInt()
                    samples[i] = sample.toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AndroidAudioAttributes.Builder()
                            .setUsage(AndroidAudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AndroidAudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                delay(durationMs.toLong() + 30)
                audioTrack.release()
            } catch (_: Exception) {
            }
        }
    }
}
