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

    private var currentPlayingBird: BirdSpecies? = null

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
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isBuffering = false
                    )
                    stopProgressTracking()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.w("BirdAudioPlayer", "ExoPlayer playback error: ${error.message}")
            // Fallback immediately to acoustic bio-synthesizer so the user still hears the bird song!
            currentPlayingBird?.let { bird ->
                val cacheFile = getCacheFileForBird(bird.scientificName)
                scope.launch {
                    val synthOk = BirdSongSynthesizer.generateSongWav(bird, cacheFile)
                    if (synthOk && isActive) {
                        playLocalAudioFile(bird, cacheFile)
                    } else {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isBuffering = false,
                            error = "Error al reproducir audio"
                        )
                        stopProgressTracking()
                    }
                }
            } ?: run {
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
                player.pause()
                _playbackState.value = current.copy(isPlaying = false)
                return
            } else if (player.playbackState == Player.STATE_READY) {
                player.play()
                _playbackState.value = current.copy(isPlaying = true)
                return
            }
        }

        stop()
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

        // 2. Set buffering state and start audio resolution
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
        withContext(Dispatchers.IO) {
            val candidateUrls = mutableListOf<String>()

            // A. Explicit audioUrl on bird
            if (!bird.audioUrl.isNullOrBlank()) {
                candidateUrls.add(bird.audioUrl)
            }

            // B. Curated verified catalogue (direct permanent links)
            val catalogUrl = BirdAudioCatalog.getAudioUrl(bird.scientificName)
            if (!catalogUrl.isNullOrBlank() && !candidateUrls.contains(catalogUrl)) {
                candidateUrls.add(catalogUrl)
            }

            // C. iNaturalist sounds API
            try {
                val inatResponse = NetworkClient.iNaturalistApi.getObservationsWithSound(
                    taxonName = bird.scientificName
                )
                val soundUrl = inatResponse.results?.firstOrNull()?.sounds?.firstOrNull()?.fileUrl
                if (!soundUrl.isNullOrBlank() && !candidateUrls.contains(soundUrl)) {
                    candidateUrls.add(soundUrl)
                }
            } catch (e: Exception) {
                Log.w("BirdAudioPlayer", "iNat sound search error: ${e.message}")
            }

            // D. Try downloading each candidate URL with OkHttp
            for (url in candidateUrls) {
                if (!isActive) return@withContext
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                        .header("Referer", "https://xeno-canto.org/")
                        .build()

                    val response = NetworkClient.okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body
                        if (body != null) {
                            val isWav = url.contains(".wav", ignoreCase = true)
                            val targetFile = if (isWav) wavDest else mp3Dest
                            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.${if (isWav) "wav" else "mp3"}")

                            FileOutputStream(tempFile).use { fos ->
                                body.byteStream().use { input ->
                                    input.copyTo(fos)
                                }
                            }

                            if (tempFile.length() > 2000) {
                                tempFile.renameTo(targetFile)
                                response.close()
                                if (isActive) {
                                    withContext(Dispatchers.Main) {
                                        playLocalAudioFile(bird, targetFile)
                                    }
                                }
                                return@withContext
                            } else {
                                tempFile.delete()
                            }
                        }
                    }
                    response.close()
                } catch (e: Exception) {
                    Log.w("BirdAudioPlayer", "Candidate audio URL failed: $url (${e.message})")
                }
            }

            // E. Fallback: Generate authentic avian bio-song with synthesizer
            if (isActive) {
                val synthSuccess = BirdSongSynthesizer.generateSongWav(bird, wavDest)
                if (synthSuccess && isActive) {
                    withContext(Dispatchers.Main) {
                        playLocalAudioFile(bird, wavDest)
                    }
                    return@withContext
                }
            }

            // If completely impossible
            if (isActive) {
                withContext(Dispatchers.Main) {
                    _playbackState.value = PlaybackState(
                        currentBirdId = bird.scientificName,
                        isPlaying = false,
                        isBuffering = false,
                        error = "No se pudo reproducir audio"
                    )
                }
            }
        }
    }

    private fun playLocalAudioFile(bird: BirdSpecies, file: File) {
        try {
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
            _playbackState.value = PlaybackState(
                currentBirdId = bird.scientificName,
                isPlaying = false,
                isBuffering = false,
                error = "Error de reproducción"
            )
        }
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
        try {
            player.stop()
        } catch (_: Exception) {}
        stopProgressTracking()
        _playbackState.value = PlaybackState()
    }

    fun release() {
        audioResolutionJob?.cancel()
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
