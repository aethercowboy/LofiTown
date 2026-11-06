package com.example.lofitown.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SkyDawnBottom
import com.example.ui.theme.SkyDawnTop
import com.example.ui.theme.SkyDayBottom
import com.example.ui.theme.SkyDayTop
import com.example.ui.theme.SkyDuskBottom
import com.example.ui.theme.SkyDuskTop
import com.example.ui.theme.SkyNightBottom
import com.example.ui.theme.SkyNightTop

enum class TimeOfDay(
    val title: String,
    val skyTop: Color,
    val skyBottom: Color,
    val ambientFilter: Color, // Tint overlay for scene
    val lampGlow: Float,      // 0.0 to 1.0 glow strength
    val windowGlow: Float,
    val description: String
) {
    DAWN(
        title = "Dawn",
        skyTop = SkyDawnTop,
        skyBottom = SkyDawnBottom,
        ambientFilter = Color(0x28E5A88B),
        lampGlow = 0.35f,
        windowGlow = 0.5f,
        description = "Misty morning mist with fresh morning coffee aroma."
    ),
    DAY(
        title = "Day",
        skyTop = SkyDayTop,
        skyBottom = SkyDayBottom,
        ambientFilter = Color(0x00000000),
        lampGlow = 0.0f,
        windowGlow = 0.0f,
        description = "Warm golden sunshine and gentle city breezes."
    ),
    DUSK(
        title = "Dusk",
        skyTop = SkyDuskTop,
        skyBottom = SkyDuskBottom,
        ambientFilter = Color(0x38C86D5B),
        lampGlow = 0.85f,
        windowGlow = 0.9f,
        description = "Velvet twilight as streetlamps flicker to life."
    ),
    NIGHT(
        title = "Night",
        skyTop = SkyNightTop,
        skyBottom = SkyNightBottom,
        ambientFilter = Color(0x600F0B18),
        lampGlow = 1.0f,
        windowGlow = 1.0f,
        description = "Peaceful midnight, glowing windows, and quiet stars."
    );

    companion object {
        fun fromMinutes(minuteOfDay: Int): TimeOfDay {
            val hour = (minuteOfDay / 60) % 24
            return when (hour) {
                in 5..8 -> DAWN
                in 9..16 -> DAY
                in 17..20 -> DUSK
                else -> NIGHT
            }
        }
    }
}
