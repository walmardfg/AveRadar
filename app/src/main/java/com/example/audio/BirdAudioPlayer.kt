package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
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
        ExoPlayer.Builder(context)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var downloadJob: Job? = null

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
            Log.w("BirdAudioPlayer", "ExoPlayer error on playback: ${error.message}")
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = "Error al reproducir audio"
            )
            stopProgressTracking()
        }
    }

    private fun getCacheFileForBird(scientificName: String): File {
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

        val cacheFile = getCacheFileForBird(bird.scientificName)
        if (cacheFile.exists() && cacheFile.length() > 2000) {
            // Already cached, play immediately
            playLocalAudioFile(bird, cacheFile)
            return
        }

        // Needs to download or resolve
        _playbackState.value = PlaybackState(
            currentBirdId = bird.scientificName,
            audioUrl = bird.audioUrl,
            isPlaying = false,
            isBuffering = true
        )

        downloadJob?.cancel()
        downloadJob = scope.launch {
            val success = downloadAndCacheAudio(bird, cacheFile)
            if (success && isActive) {
                playLocalAudioFile(bird, cacheFile)
            } else if (isActive) {
                _playbackState.value = PlaybackState(
                    currentBirdId = bird.scientificName,
                    isPlaying = false,
                    isBuffering = false,
                    error = "No se pudo descargar el audio"
                )
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

    private suspend fun downloadAndCacheAudio(bird: BirdSpecies, destination: File): Boolean {
        return withContext(Dispatchers.IO) {
            // Try list of potential URLs
            val candidateUrls = mutableListOf<String>()

            // 1. Initial bird URL
            val directUrl = bird.audioUrl
            if (!directUrl.isNullOrBlank()) {
                candidateUrls.add(directUrl)
            }

            // 2. Fetch from Xeno-Canto API if needed
            try {
                val parts = bird.scientificName.trim().split(" ")
                val genus = parts.getOrNull(0)?.trim() ?: ""
                val species = parts.getOrNull(1)?.trim() ?: ""

                val response = NetworkClient.xenoCantoApi.searchRecordings(query = "$genus $species")
                val recordings = response.recordings ?: emptyList()

                val matched = recordings.filter { rec ->
                    val recGen = rec.gen?.trim() ?: ""
                    val recSp = rec.sp?.trim() ?: ""
                    recGen.equals(genus, ignoreCase = true) &&
                            (species.isBlank() || recSp.contains(species, ignoreCase = true))
                }.sortedWith(
                    compareBy(
                        { if (it.quality == "A") 0 else if (it.quality == "B") 1 else 2 },
                        { if (it.type?.contains("song", ignoreCase = true) == true) 0 else 1 }
                    )
                )

                for (rec in matched) {
                    if (!rec.file.isNullOrBlank()) {
                        val f = rec.file
                        val full = if (f.startsWith("//")) "https:$f" else f
                        if (!candidateUrls.contains(full)) candidateUrls.add(full)
                    }
                    if (!rec.id.isNullOrBlank()) {
                        val dlUrl = "https://xeno-canto.org/${rec.id}/download"
                        if (!candidateUrls.contains(dlUrl)) candidateUrls.add(dlUrl)
                    }
                }
            } catch (e: Exception) {
                Log.w("BirdAudioPlayer", "Xeno-Canto API lookup error: ${e.message}")
            }

            // Attempt downloading from candidates
            for (url in candidateUrls) {
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
                            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.mp3")
                            FileOutputStream(tempFile).use { fos ->
                                body.byteStream().use { input ->
                                    input.copyTo(fos)
                                }
                            }
                            if (tempFile.length() > 2000) {
                                tempFile.renameTo(destination)
                                response.close()
                                return@withContext true
                            } else {
                                tempFile.delete()
                            }
                        }
                    }
                    response.close()
                } catch (e: Exception) {
                    Log.w("BirdAudioPlayer", "Failed downloading from $url: ${e.message}")
                }
            }

            false
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
        downloadJob?.cancel()
        downloadJob = null
        try {
            player.stop()
        } catch (_: Exception) {}
        stopProgressTracking()
        _playbackState.value = PlaybackState()
    }

    fun release() {
        downloadJob?.cancel()
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
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
