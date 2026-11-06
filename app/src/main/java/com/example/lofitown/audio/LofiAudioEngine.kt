package com.example.lofitown.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class LofiAudioEngine {
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Playback state
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.75f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _rainVolume = MutableStateFlow(0.35f)
    val rainVolume: StateFlow<Float> = _rainVolume.asStateFlow()

    private val _vinylVolume = MutableStateFlow(0.25f)
    val vinylVolume: StateFlow<Float> = _vinylVolume.asStateFlow()

    private val _visualizerAmplitudes = MutableStateFlow(List(12) { 0.1f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private val tracks = LofiTrack.ALL_TRACKS

    val currentTrack: LofiTrack
        get() = tracks[_currentTrackIndex.value]

    init {
        startEngine()
    }

    fun startEngine() {
        if (synthJob?.isActive == true) return
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            synthJob = scope.launch {
                synthesizerLoop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % tracks.size
    }

    fun previousTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
    }

    fun selectTrack(index: Int) {
        if (index in tracks.indices) {
            _currentTrackIndex.value = index
        }
    }

    fun setMusicVolume(volume: Float) {
        _musicVolume.value = volume.coerceIn(0f, 1f)
    }

    fun setRainVolume(volume: Float) {
        _rainVolume.value = volume.coerceIn(0f, 1f)
    }

    fun setVinylVolume(volume: Float) {
        _vinylVolume.value = volume.coerceIn(0f, 1f)
    }

    private suspend fun synthesizerLoop() {
        val chunkSamples = 1024
        val audioBuffer = ShortArray(chunkSamples)

        var sampleIndex = 0L
        var chordIndex = 0
        var chordSampleCounter = 0
        var beatSampleCounter = 0
        var beatIndex = 0

        // Filter states for rain and warmth
        var rainFilterState = 0.0
        var lpFilterState = 0.0

        val twoPi = 2.0 * PI

        while (scope.isActive) {
            val playing = _isPlaying.value
            val musicVol = _musicVolume.value
            val rainVol = _rainVolume.value
            val vinylVol = _vinylVolume.value

            val track = tracks[_currentTrackIndex.value]
            val samplesPerBeat = (sampleRate * 60) / track.bpm
            val beatsPerChord = 4 // 1 measure per chord
            val samplesPerChord = samplesPerBeat * beatsPerChord

            var chunkRmsSum = 0.0

            for (i in 0 until chunkSamples) {
                var totalSample = 0.0

                if (playing) {
                    val currentChordVoicing = track.chordFrequencies[chordIndex % track.chordFrequencies.size]
                    val chordTime = chordSampleCounter.toDouble() / sampleRate
                    val chordDuration = samplesPerChord.toDouble() / sampleRate

                    // --- 1. Warm Rhodes / Electric Piano Chords ---
                    var chordSignal = 0.0
                    if (musicVol > 0.001f) {
                        // Subtle wow/flutter tape wobble
                        val tapeWobble = 1.0 + 0.002 * sin(twoPi * 0.45 * (sampleIndex.toDouble() / sampleRate))

                        // Piano note decay envelope
                        val envelope = exp(-1.8 * (chordTime / chordDuration.coerceAtLeast(0.1)))
                            .coerceIn(0.0, 1.0)

                        for (freq in currentChordVoicing) {
                            val f = freq.toDouble() * tapeWobble
                            val phase = twoPi * f * (sampleIndex.toDouble() / sampleRate)

                            // Fundamental + mellow warm 2nd and 3rd harmonics + subtle bell tine
                            val fundamental = sin(phase)
                            val harmonic2 = 0.45 * sin(phase * 2.0)
                            val harmonic3 = 0.18 * sin(phase * 3.0)
                            val bell = 0.08 * sin(phase * 7.0) * exp(-8.0 * chordTime)

                            chordSignal += (fundamental + harmonic2 + harmonic3 + bell) * envelope
                        }
                        // Gentle tremolo modulation (4 Hz)
                        val tremolo = 1.0 + 0.12 * sin(twoPi * 3.8 * (sampleIndex.toDouble() / sampleRate))
                        chordSignal = (chordSignal / currentChordVoicing.size) * tremolo * 0.55 * musicVol
                    }

                    // --- 2. Cozy Lofi Beat (Soft Kick on 1 & 3, Brush Snare on 2 & 4) ---
                    var drumSignal = 0.0
                    if (musicVol > 0.001f) {
                        val beatTime = beatSampleCounter.toDouble() / sampleRate
                        if (beatIndex % 2 == 0) {
                            // Soft Muffled Kick
                            if (beatTime < 0.18) {
                                val kickFreq = 110.0 * exp(-18.0 * beatTime) + 42.0
                                val kickEnv = exp(-14.0 * beatTime)
                                drumSignal += sin(twoPi * kickFreq * beatTime) * kickEnv * 0.45 * musicVol
                            }
                        } else {
                            // Soft Brush Snare / Rim
                            if (beatTime < 0.15) {
                                val noise = (Random.nextDouble() * 2.0 - 1.0)
                                val snareEnv = exp(-20.0 * beatTime)
                                drumSignal += noise * snareEnv * 0.22 * musicVol
                            }
                        }
                    }

                    // --- 3. Ambient Rain Sound (Smoothed filtered noise) ---
                    var rainSignal = 0.0
                    if (rainVol > 0.001f) {
                        val whiteNoise = Random.nextDouble() * 2.0 - 1.0
                        // Low-pass filter for cozy muffled rain
                        rainFilterState += 0.08 * (whiteNoise - rainFilterState)
                        val rainSwell = 1.0 + 0.2 * sin(twoPi * 0.15 * (sampleIndex.toDouble() / sampleRate))
                        rainSignal = rainFilterState * 0.35 * rainSwell * rainVol
                    }

                    // --- 4. Vinyl Crackle & Surface Hiss ---
                    var vinylSignal = 0.0
                    if (vinylVol > 0.001f) {
                        // Subtle surface hiss
                        val hiss = (Random.nextDouble() * 2.0 - 1.0) * 0.04
                        // Occasional dust click / pop (0.04% chance per sample)
                        val click = if (Random.nextFloat() < 0.0004f) {
                            (Random.nextDouble() * 2.0 - 1.0) * 0.45
                        } else 0.0
                        vinylSignal = (hiss + click) * vinylVol
                    }

                    totalSample = chordSignal + drumSignal + rainSignal + vinylSignal

                    // Gentle master analog saturation/limiter
                    lpFilterState += 0.7 * (totalSample - lpFilterState)
                    totalSample = lpFilterState.coerceIn(-1.0, 1.0)

                    // Increment counters
                    sampleIndex++
                    chordSampleCounter++
                    beatSampleCounter++

                    if (beatSampleCounter >= samplesPerBeat) {
                        beatSampleCounter = 0
                        beatIndex = (beatIndex + 1) % 4
                    }

                    if (chordSampleCounter >= samplesPerChord) {
                        chordSampleCounter = 0
                        chordIndex = (chordIndex + 1) % track.chordFrequencies.size
                    }
                }

                chunkRmsSum += totalSample * totalSample
                // Convert to 16-bit signed PCM
                val pcmValue = (totalSample * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
                audioBuffer[i] = pcmValue
            }

            // Write to AudioTrack
            audioTrack?.write(audioBuffer, 0, chunkSamples)

            // Update visualizer state periodically
            val rms = Math.sqrt(chunkRmsSum / chunkSamples).toFloat().coerceIn(0.05f, 1.0f)
            val baseLevels = List(12) { idx ->
                val freqFactor = sin(idx.toFloat() * 0.8f + (sampleIndex / 5000f)).toFloat() * 0.3f
                (rms * (0.6f + idx * 0.04f) + freqFactor).coerceIn(0.08f, 0.95f)
            }
            _visualizerAmplitudes.value = baseLevels
        }
    }

    fun release() {
        synthJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }
}
