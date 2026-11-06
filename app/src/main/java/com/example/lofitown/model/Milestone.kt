package com.example.lofitown.model

data class Milestone(
    val id: String,
    val title: String,
    val requirement: String,
    val rewardZen: Int,
    val isCompleted: Boolean = false,
    val category: String = "Cozy Life"
) {
    companion object {
        fun defaultMilestones(): List<Milestone> = listOf(
            Milestone("m_first_cafe", "First Aroma", "Build a Lofi Roastery cafe", 5),
            Milestone("m_vinyl", "Analog Warmth", "Build the Vinyl & Tapes lounge", 8),
            Milestone("m_bookstore", "Quiet Reading", "Build the Rainy Bookstore", 12),
            Milestone("m_ramen", "Midnight Supper", "Build the Midnight Ramen stall", 15),
            Milestone("m_pet_cat", "Feline Companion", "Tap Mochi the Cat 3 times", 10),
            Milestone("m_dusk_watcher", "Golden Hour", "Witness the transition into Dusk", 8),
            Milestone("m_night_sky", "Starlit Town", "Watch the cozy town under the Night sky", 10),
            Milestone("m_rain_listener", "Petrichor", "Listen to a gentle rain shower", 8),
            Milestone("m_focus_zen", "Deep Focus", "Complete a 15-minute Chill Study session", 20),
            Milestone("m_vibes_10k", "Vibe Master", "Accumulate 10,000 total Vibes", 25)
        )
    }
}
