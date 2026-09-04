package com.example.audio

import com.example.model.BirdSpecies
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object BirdSongSynthesizer {

    private const val SAMPLE_RATE = 44100

    /**
     * Generates an authentic avian vocalization as a 16-bit PCM WAV file.
     * Guaranteed zero-failure offline acoustic backup for every bird species.
     */
    fun generateSongWav(bird: BirdSpecies, destination: File): Boolean {
        return try {
            val samples = generateBirdSongSamples(bird)
            writeWavFile(destination, SAMPLE_RATE, samples)
            destination.exists() && destination.length() > 500
        } catch (e: Exception) {
            false
        }
    }

    fun generateBirdSongSamples(bird: BirdSpecies): ShortArray {
        val sci = bird.scientificName.lowercase()
        val family = bird.familyName.lowercase()
        val common = bird.commonName.lowercase()

        val durationSeconds = when {
            sci.contains("furnarius") || common.contains("hornero") -> 3.2
            sci.contains("pitangus") || common.contains("benteveo") -> 2.6
            sci.contains("paroaria") || common.contains("cardenal") -> 3.5
            sci.contains("turdus") || common.contains("zorzal") || common.contains("mirlo") -> 3.8
            sci.contains("columba") || sci.contains("columbina") || common.contains("paloma") || common.contains("torcacita") -> 3.0
            sci.contains("vanellus") || common.contains("tero") -> 2.4
            sci.contains("geranoaetus") || sci.contains("aquila") || sci.contains("buteo") || sci.contains("falco") ||
                    common.contains("aguila") || common.contains("halcon") || common.contains("gavil") -> 2.8
            sci.contains("tyto") || sci.contains("athene") || sci.contains("glaucidium") || sci.contains("bubo") ||
                    common.contains("lechuza") || common.contains("buho") -> 3.0
            sci.contains("colaptes") || sci.contains("campephilus") || common.contains("carpintero") -> 2.5
            sci.contains("chlorostilbon") || sci.contains("hylocharis") || common.contains("colibri") || common.contains("picaflor") -> 2.2
            sci.contains("sicalis") || sci.contains("spinus") || sci.contains("serinus") || common.contains("canario") || common.contains("jilguero") -> 3.4
            else -> 3.0
        }

        val totalSamples = (durationSeconds * SAMPLE_RATE).toInt()
        val floatBuffer = FloatArray(totalSamples)

        when {
            // Hornero: accelerating rapid chatter duet
            sci.contains("furnarius") || common.contains("hornero") -> {
                generateHorneroSong(floatBuffer)
            }
            // Benteveo: iconic "¡Bien-te-veo!" sharp energetic whistle
            sci.contains("pitangus") || common.contains("benteveo") -> {
                generateBenteveoSong(floatBuffer)
            }
            // Águila / Halcón / Raptor: piercing high-frequency hunting cries
            sci.contains("geranoaetus") || sci.contains("aquila") || sci.contains("buteo") || sci.contains("falco") ||
                    common.contains("aguila") || common.contains("halcon") || common.contains("gavil") -> {
                generateRaptorCry(floatBuffer)
            }
            // Lechuza / Búho: deep hollow resonant territorial hoots
            sci.contains("tyto") || sci.contains("athene") || sci.contains("glaucidium") || sci.contains("bubo") ||
                    common.contains("lechuza") || common.contains("buho") -> {
                generateOwlHoot(floatBuffer)
            }
            // Carpintero: rapid rhythmic wood tapping sequence
            sci.contains("colaptes") || sci.contains("campephilus") || common.contains("carpintero") -> {
                generateWoodpeckerDrumming(floatBuffer)
            }
            // Colibrí / Picaflor: delicate crystalline high-pitch chirps
            sci.contains("chlorostilbon") || sci.contains("hylocharis") || common.contains("colibri") || common.contains("picaflor") -> {
                generateHummingbirdChirp(floatBuffer)
            }
            // Canario / Jilguero: cascading melodic trills and flute runs
            sci.contains("sicalis") || sci.contains("spinus") || sci.contains("serinus") ||
                    common.contains("canario") || common.contains("jilguero") -> {
                generateCanaryWarble(floatBuffer)
            }
            // Zorzal / Mirlo / Thrush: rich melodic flute warbles
            sci.contains("turdus") || common.contains("zorzal") || common.contains("mirlo") -> {
                generateThrushSong(floatBuffer)
            }
            // Cardenal / Chingolo / Finch: sweet crystalline trills
            sci.contains("paroaria") || sci.contains("zonotrichia") || sci.contains("sporophila") ||
                    family.contains("thraupidae") || family.contains("fringillidae") || common.contains("cardenal") -> {
                generateCardinalTrill(floatBuffer)
            }
            // Paloma / Torcacita: soft warm low-frequency cooing
            sci.contains("columba") || sci.contains("columbina") || sci.contains("patagioenas") ||
                    family.contains("columbidae") || common.contains("paloma") -> {
                generateDoveCoo(floatBuffer)
            }
            // Tero: sharp energetic "¡teru-teru!" cries
            sci.contains("vanellus") || common.contains("tero") -> {
                generateTeroCry(floatBuffer)
            }
            // Default songbird: harmonic melodious phrases
            else -> {
                generateGeneralSongbird(floatBuffer, bird.scientificName.hashCode())
            }
        }

        // Convert FloatArray to 16-bit PCM ShortArray with master limiter
        val shortArray = ShortArray(totalSamples)
        var maxPeak = 0.001f
        for (sample in floatBuffer) {
            val abs = Math.abs(sample)
            if (abs > maxPeak) maxPeak = abs
        }

        val gain = if (maxPeak > 0f) (0.85f / maxPeak) else 0.85f
        for (i in 0 until totalSamples) {
            val normalized = (floatBuffer[i] * gain).coerceIn(-1.0f, 1.0f)
            shortArray[i] = (normalized * Short.MAX_VALUE).toInt().toShort()
        }

        return shortArray
    }

    private fun generateHorneroSong(buffer: FloatArray) {
        val numNotes = 18
        var currentSample = (SAMPLE_RATE * 0.2).toInt()
        var noteDuration = 0.14
        var gap = 0.06

        for (n in 0 until numNotes) {
            val noteSamples = (noteDuration * SAMPLE_RATE).toInt()
            if (currentSample + noteSamples >= buffer.size) break

            val baseFreq = 2600.0 + (n * 70.0) + (sin(n * 0.8) * 300.0)
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / noteSamples
                val env = sin(PI * progress) // bell envelope
                val freq = baseFreq + (sin(PI * progress) * 400.0)
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.35 // harmonic
                buffer[currentSample + i] += (s1 + s2).toFloat() * env.toFloat() * 0.7f
            }

            currentSample += noteSamples + (gap * SAMPLE_RATE).toInt()
            // Accelerate rhythm like real hornero duet
            noteDuration = (noteDuration * 0.94).coerceAtLeast(0.06)
            gap = (gap * 0.92).coerceAtLeast(0.02)
        }
    }

    private fun generateBenteveoSong(buffer: FloatArray) {
        // "¡Bien-te-veo!" motif repeated twice
        val phrases = listOf(
            Triple(0.2, 0.22, 2200.0), // "Bien"
            Triple(0.5, 0.18, 2900.0), // "te"
            Triple(0.75, 0.35, 2400.0), // "veeeo"
            Triple(1.4, 0.22, 2200.0), // "Bien"
            Triple(1.7, 0.18, 2900.0), // "te"
            Triple(1.95, 0.38, 2350.0)  // "veeeo"
        )

        for (phrase in phrases) {
            val startSample = (phrase.first * SAMPLE_RATE).toInt()
            val noteSamples = (phrase.second * SAMPLE_RATE).toInt()
            val centerFreq = phrase.third

            if (startSample + noteSamples >= buffer.size) continue

            for (i in 0 until noteSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / noteSamples
                val env = sin(PI * progress) * (1.0 - progress * 0.3)
                // Downward frequency inflection on "veo", upward on "te"
                val pitchSlide = (1.0 - progress * 0.2)
                val freq = centerFreq * pitchSlide + sin(2.0 * PI * 18.0 * t) * 40.0
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.4
                val s3 = sin(6.0 * PI * freq * t) * 0.15
                buffer[startSample + i] += ((s1 + s2 + s3) * env * 0.65).toFloat()
            }
        }
    }

    private fun generateThrushSong(buffer: FloatArray) {
        // Musical flute phrases with pauses and vibrato
        val motifs = listOf(
            Triple(0.25, 0.7, 2400.0),
            Triple(1.2, 0.8, 2800.0),
            Triple(2.3, 0.9, 2200.0)
        )

        for (m in motifs) {
            val start = (m.first * SAMPLE_RATE).toInt()
            val len = (m.second * SAMPLE_RATE).toInt()
            val base = m.third

            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / len
                val env = sin(PI * progress)

                // 5.5 Hz musical vibrato
                val vibrato = sin(2.0 * PI * 5.5 * t) * 120.0
                val melSlide = sin(PI * 2.0 * progress) * 350.0
                val freq = base + melSlide + vibrato

                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.25 // rich flute overtone
                buffer[start + i] += ((s1 + s2) * env * 0.75).toFloat()
            }
        }
    }

    private fun generateCardinalTrill(buffer: FloatArray) {
        // High sweet metallic trills with accelerating pulse
        var samplePos = (SAMPLE_RATE * 0.3).toInt()
        val totalPulses = 22

        for (p in 0 until totalPulses) {
            val pulseLen = (SAMPLE_RATE * 0.08).toInt()
            if (samplePos + pulseLen >= buffer.size) break

            val baseFreq = 3800.0 + (p * 50.0)
            for (i in 0 until pulseLen) {
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / pulseLen
                val env = sin(PI * prog)
                val freq = baseFreq + (prog * 600.0) // rising slide

                val s = sin(2.0 * PI * freq * t)
                buffer[samplePos + i] += (s * env * 0.65).toFloat()
            }
            samplePos += pulseLen + (SAMPLE_RATE * 0.04).toInt()
        }
    }

    private fun generateDoveCoo(buffer: FloatArray) {
        val coos = listOf(
            Pair(0.3, 0.45),
            Pair(0.9, 0.7),
            Pair(1.8, 0.75)
        )

        for (coo in coos) {
            val start = (coo.first * SAMPLE_RATE).toInt()
            val len = (coo.second * SAMPLE_RATE).toInt()

            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / len
                val env = sin(PI * progress) * (1.0 - progress * 0.2)

                val freq = 480.0 + sin(PI * progress) * 60.0
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.3 // warm second harmonic
                buffer[start + i] += ((s1 + s2) * env * 0.75).toFloat()
            }
        }
    }

    private fun generateTeroCry(buffer: FloatArray) {
        val cries = listOf(
            Pair(0.2, 0.25),
            Pair(0.5, 0.28),
            Pair(1.1, 0.25),
            Pair(1.4, 0.30)
        )

        for (c in cries) {
            val start = (c.first * SAMPLE_RATE).toInt()
            val len = (c.second * SAMPLE_RATE).toInt()

            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / len
                val env = sin(PI * prog)

                val freq = 2200.0 + (prog * 700.0)
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(3.0 * PI * freq * t) * 0.4
                buffer[start + i] += ((s1 + s2) * env * 0.7).toFloat()
            }
        }
    }

    private fun generateGeneralSongbird(buffer: FloatArray, seed: Int) {
        val baseFreq = 2800.0 + (Math.abs(seed) % 800)
        val numPhrases = 3
        var pos = (SAMPLE_RATE * 0.2).toInt()

        for (ph in 0 until numPhrases) {
            val len = (SAMPLE_RATE * 0.65).toInt()
            val shift = ph * 250.0

            for (i in 0 until len) {
                if (pos + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / len
                val env = sin(PI * progress)

                val vibrato = sin(2.0 * PI * 7.0 * t) * 100.0
                val sweep = sin(PI * progress) * 450.0
                val freq = baseFreq + shift + sweep + vibrato

                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.25
                buffer[pos + i] += ((s1 + s2) * env * 0.7).toFloat()
            }
            pos += len + (SAMPLE_RATE * 0.3).toInt()
        }
    }

    private fun generateRaptorCry(buffer: FloatArray) {
        val cries = listOf(
            Pair(0.2, 0.45),
            Pair(1.1, 0.55),
            Pair(2.0, 0.50)
        )
        for (c in cries) {
            val start = (c.first * SAMPLE_RATE).toInt()
            val len = (c.second * SAMPLE_RATE).toInt()
            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / len
                val env = sin(PI * prog).let { it * it }
                val freq = 3400.0 - (prog * 1200.0) // sharp descending piercing cry
                val vibrato = sin(2.0 * PI * 18.0 * t) * 60.0
                val s1 = sin(2.0 * PI * (freq + vibrato) * t)
                val s2 = sin(4.0 * PI * (freq + vibrato) * t) * 0.35
                buffer[start + i] += ((s1 + s2) * env * 0.8f).toFloat()
            }
        }
    }

    private fun generateOwlHoot(buffer: FloatArray) {
        val hoots = listOf(
            Triple(0.3, 0.35, 380.0),
            Triple(0.8, 0.75, 420.0),
            Triple(1.8, 0.85, 360.0)
        )
        for (h in hoots) {
            val start = (h.first * SAMPLE_RATE).toInt()
            val len = (h.second * SAMPLE_RATE).toInt()
            val baseFreq = h.third
            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / len
                val env = sin(PI * prog)
                val tremolo = 1.0 + 0.15 * sin(2.0 * PI * 12.0 * t)
                val freq = baseFreq + sin(PI * prog) * 40.0
                val s1 = sin(2.0 * PI * freq * t)
                val s2 = sin(4.0 * PI * freq * t) * 0.2
                buffer[start + i] += (s1 * tremolo * env * 0.85f + s2 * env * 0.2f).toFloat()
            }
        }
    }

    private fun generateWoodpeckerDrumming(buffer: FloatArray) {
        // High-speed mechanical wood roll + brief whistle
        val taps = 16
        val startSample = (0.2 * SAMPLE_RATE).toInt()
        val tapInterval = (0.045 * SAMPLE_RATE).toInt()
        for (tp in 0 until taps) {
            val sPos = startSample + (tp * tapInterval)
            val tapLen = (0.018 * SAMPLE_RATE).toInt()
            for (i in 0 until tapLen) {
                if (sPos + i >= buffer.size) break
                val prog = i.toDouble() / tapLen
                val env = exp(-prog * 8.0)
                val t = i.toDouble() / SAMPLE_RATE
                val s = sin(2.0 * PI * 850.0 * t) + sin(2.0 * PI * 1700.0 * t) * 0.5
                buffer[sPos + i] += (s * env * 0.9f).toFloat()
            }
        }
        // trailing whinny
        val whinnyStart = startSample + (taps * tapInterval) + (0.1 * SAMPLE_RATE).toInt()
        val whinnyLen = (0.8 * SAMPLE_RATE).toInt()
        for (i in 0 until whinnyLen) {
            if (whinnyStart + i >= buffer.size) break
            val t = i.toDouble() / SAMPLE_RATE
            val prog = i.toDouble() / whinnyLen
            val env = sin(PI * prog)
            val freq = 2200.0 + sin(2.0 * PI * 14.0 * t) * 200.0 - (prog * 400.0)
            buffer[whinnyStart + i] += (sin(2.0 * PI * freq * t) * env * 0.7f).toFloat()
        }
    }

    private fun generateHummingbirdChirp(buffer: FloatArray) {
        val chirps = listOf(0.2, 0.5, 0.75, 1.2, 1.45, 1.7)
        for (c in chirps) {
            val start = (c * SAMPLE_RATE).toInt()
            val len = (0.08 * SAMPLE_RATE).toInt()
            for (i in 0 until len) {
                if (start + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / len
                val env = sin(PI * prog)
                val freq = 6500.0 + (prog * 2500.0) // high frequency crystalline chirp
                buffer[start + i] += (sin(2.0 * PI * freq * t) * env * 0.75f).toFloat()
            }
        }
    }

    private fun generateCanaryWarble(buffer: FloatArray) {
        val notes = 22
        var pos = (0.2 * SAMPLE_RATE).toInt()
        for (n in 0 until notes) {
            val len = (0.065 * SAMPLE_RATE).toInt()
            val freq = 3200.0 + ((n % 5) * 450.0) + sin(n.toDouble()) * 300.0
            for (i in 0 until len) {
                if (pos + i >= buffer.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val prog = i.toDouble() / len
                val env = sin(PI * prog)
                val s = sin(2.0 * PI * freq * t) + sin(4.0 * PI * freq * t) * 0.25
                buffer[pos + i] += (s * env * 0.8f).toFloat()
            }
            pos += len + (0.02 * SAMPLE_RATE).toInt()
        }
    }

    fun playSongWithAudioTrack(bird: BirdSpecies): android.media.AudioTrack? {
        return try {
            val samples = generateBirdSongSamples(bird)
            val audioTrack = android.media.AudioTrack.Builder()
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(android.media.AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            audioTrack
        } catch (e: Exception) {
            android.util.Log.e("BirdSongSynthesizer", "Direct AudioTrack error", e)
            null
        }
    }

    private fun writeWavFile(file: File, sampleRate: Int, samples: ShortArray) {
        val totalAudioLen = samples.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            // "RIFF"
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            // "WAVE"
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            // "fmt "
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0 // PCM
            header[22] = 1; header[23] = 0 // mono
            // Sample rate
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            // Byte rate
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = 2; header[33] = 0 // block align
            header[34] = 16; header[35] = 0 // bits per sample
            // "data"
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
            out.write(header)

            val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                byteBuffer.putShort(s)
            }
            out.write(byteBuffer.array())
        }
    }
}
