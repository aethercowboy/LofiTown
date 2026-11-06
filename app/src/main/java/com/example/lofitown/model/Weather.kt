package com.example.lofitown.model

enum class Weather(
    val title: String,
    val icon: String,
    val description: String,
    val rainIntensity: Float // 0.0 to 1.0
) {
    CLEAR(
        title = "Clear & Calm",
        icon = "☀️",
        description = "Soft gentle breeze rustling through the leaves.",
        rainIntensity = 0.0f
    ),
    GENTLE_RAIN(
        title = "Gentle Rain",
        icon = "🌧️",
        description = "Rhythmic raindrops pattering softly on cafe windows.",
        rainIntensity = 0.75f
    ),
    SAKURA_DRIFT(
        title = "Sakura Petals",
        icon = "🌸",
        description = "Delicate pink cherry blossom petals floating on the wind.",
        rainIntensity = 0.0f
    ),
    COZY_MIST(
        title = "Cozy Mist",
        icon = "🌫️",
        description = "Quiet fog drifting over the river and cobblestones.",
        rainIntensity = 0.15f
    )
}
