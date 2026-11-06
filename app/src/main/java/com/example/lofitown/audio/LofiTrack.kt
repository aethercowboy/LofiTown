package com.example.lofitown.audio

data class LofiTrack(
    val id: Int,
    val title: String,
    val artist: String = "Lofi Haven Beats",
    val key: String,
    val bpm: Int,
    val chordFrequencies: List<List<Float>> // list of chord voicings (frequencies in Hz)
) {
    companion object {
        // Standard frequency mappings
        private val C3 = 130.81f
        private val D3 = 146.83f
        private val Eb3 = 155.56f
        private val E3 = 164.81f
        private val F3 = 174.61f
        private val G3 = 196.00f
        private val Ab3 = 207.65f
        private val A3 = 220.00f
        private val Bb3 = 233.08f
        private val B3 = 246.94f

        private val C4 = 261.63f
        private val D4 = 293.66f
        private val Eb4 = 311.13f
        private val E4 = 329.63f
        private val F4 = 349.23f
        private val G4 = 392.00f
        private val Ab4 = 415.30f
        private val A4 = 440.00f
        private val Bb4 = 466.16f
        private val B4 = 493.88f
        private val C5 = 523.25f
        private val D5 = 587.33f
        private val Eb5 = 622.25f

        val ALL_TRACKS = listOf(
            LofiTrack(
                id = 0,
                title = "Midnight Coffee",
                key = "Eb Major 9",
                bpm = 64,
                chordFrequencies = listOf(
                    listOf(Eb3, G3, Bb3, D4, F4),    // Ebmaj9
                    listOf(C3, Eb3, G3, Bb3, D4),    // Cm9
                    listOf(F3, Ab3, C4, Eb4, G4),    // Fm9
                    listOf(Bb3, D4, Ab4, C5)         // Bb13
                )
            ),
            LofiTrack(
                id = 1,
                title = "Rainy Daydreams",
                key = "F Major 7",
                bpm = 58,
                chordFrequencies = listOf(
                    listOf(F3, A3, C4, E4),          // Fmaj7
                    listOf(E3, G3, B3, D4),          // Em7
                    listOf(D3, F3, A3, C4),          // Dm7
                    listOf(C3, E3, G3, B3, D4)       // Cmaj9
                )
            ),
            LofiTrack(
                id = 2,
                title = "Dusk in the Alley",
                key = "Ab Major 7",
                bpm = 68,
                chordFrequencies = listOf(
                    listOf(Ab3, C4, Eb4, G4),        // Abmaj7
                    listOf(G3, Bb3, D4, F4),         // Gm7
                    listOf(F3, Ab3, C4, Eb4),        // Fm7
                    listOf(Bb3, D4, F4, Ab4, C5)     // Bbm9
                )
            ),
            LofiTrack(
                id = 3,
                title = "Cat on the Roof",
                key = "G Major 9",
                bpm = 62,
                chordFrequencies = listOf(
                    listOf(G3, B3, D4, F4 + 18f, A4), // Gmaj9
                    listOf(B3, D4, F4 + 18f, A4),     // Bm7
                    listOf(C3, E3, G3, B3, D4),       // Cmaj9
                    listOf(D3, G3, A3, C4, E4)        // D7sus4
                )
            )
        )
    }
}
