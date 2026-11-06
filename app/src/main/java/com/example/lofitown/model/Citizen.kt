package com.example.lofitown.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LofiCoral
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSkyBlue
import com.example.ui.theme.LofiYellowGlow

data class Citizen(
    val id: String,
    val name: String,
    val role: String,
    var gridX: Float,
    var gridY: Float,
    var targetX: Float,
    var targetY: Float,
    val clothesColor: Color,
    val hairColor: Color,
    val isCat: Boolean = false,
    val thoughts: List<String>
) {
    fun randomThought(): String = thoughts.random()

    companion object {
        fun createDefaultCitizens(): List<Citizen> = listOf(
            Citizen(
                id = "c_maya",
                name = "Maya",
                role = "Lofi Listener",
                gridX = 1f,
                gridY = 2f,
                targetX = 1f,
                targetY = 2f,
                clothesColor = LofiLavender,
                hairColor = Color(0xFF2C2436),
                thoughts = listOf(
                    "This electric piano chord feels like a warm knitted blanket.",
                    "Raindrops hitting the cafe awning make the best drum beat.",
                    "I could listen to this track on repeat for hours.",
                    "Taking a slow breath... feeling peaceful."
                )
            ),
            Citizen(
                id = "c_leo",
                name = "Leo",
                role = "Bookworm",
                gridX = 2f,
                gridY = 1f,
                targetX = 2f,
                targetY = 1f,
                clothesColor = LofiPeach,
                hairColor = Color(0xFF5A3E2B),
                thoughts = listOf(
                    "Nothing beats the smell of old second-hand books.",
                    "Found a handwritten note tucked inside page 142...",
                    "Just finished chapter 7. Time for another cup of tea.",
                    "The quiet in this town is so healing."
                )
            ),
            Citizen(
                id = "c_mochi",
                name = "Mochi",
                role = "Town Cat",
                gridX = 2f,
                gridY = 2f,
                targetX = 2f,
                targetY = 2f,
                clothesColor = LofiYellowGlow,
                hairColor = Color(0xFFFAF0E6),
                isCat = true,
                thoughts = listOf(
                    "Purrrrr... *curls up in warm square of sunlight*",
                    "Mew! *gently headbutts your ankle*",
                    "*Stretches paws forward, yawns softly*",
                    "The fish broth from the ramen shop smells extraordinary."
                )
            ),
            Citizen(
                id = "c_chloe",
                name = "Chloe",
                role = "Barista",
                gridX = 0f,
                gridY = 2f,
                targetX = 0f,
                targetY = 2f,
                clothesColor = LofiMatcha,
                hairColor = Color(0xFF4A3528),
                thoughts = listOf(
                    "Slow-drip brew takes 4 hours, but patience tastes sweetest.",
                    "Steamed oat milk with a hint of cinnamon and nutmeg.",
                    "Welcome! Take your time, there's nowhere you need to rush.",
                    "Watching the steam curl up into the cool air."
                )
            ),
            Citizen(
                id = "c_sam",
                name = "Sam",
                role = "Vinyl Collector",
                gridX = 3f,
                gridY = 2f,
                targetX = 3f,
                targetY = 2f,
                clothesColor = LofiSkyBlue,
                hairColor = Color(0xFF1E1E24),
                thoughts = listOf(
                    "Analog warmth just has a soul you can't describe.",
                    "Look at this mint condition Bossa Nova 7-inch!",
                    "A gentle turntable wobble adds all the character.",
                    "Headphones on, world tuned out."
                )
            ),
            Citizen(
                id = "c_kaito",
                name = "Kaito",
                role = "Night Stargazer",
                gridX = 2f,
                gridY = 3f,
                targetX = 2f,
                targetY = 3f,
                clothesColor = LofiCoral,
                hairColor = Color(0xFF382F45),
                thoughts = listOf(
                    "Look at that constellation rising over the river.",
                    "In the grand universe, this quiet moment is a little gem.",
                    "The night breeze carries the scent of pine and cedar.",
                    "Time moves so gently when you watch the stars."
                )
            )
        )
    }
}
