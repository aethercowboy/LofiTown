package com.example.lofitown.model

enum class TerrainType(
    val title: String,
    val cost: Long,
    val description: String,
    val isWalkable: Boolean
) {
    MEADOW(
        title = "Grassy Meadow",
        cost = 0,
        description = "Soft swaying pixel grass with tiny wildflowers.",
        isWalkable = true
    ),
    COBBLESTONE(
        title = "Cobblestone Path",
        cost = 5,
        description = "Hand-laid smooth gray stones for cozy town strolls.",
        isWalkable = true
    ),
    RIVER(
        title = "Peaceful Stream",
        cost = 10,
        description = "Gentle murmuring river with shimmering light reflections.",
        isWalkable = false
    ),
    BRIDGE(
        title = "Wooden Bridge",
        cost = 25,
        description = "Rustic cedar planks crossing over the tranquil stream.",
        isWalkable = true
    ),
    SAKURA_TREE(
        title = "Sakura Tree",
        cost = 40,
        description = "Pink cherry blossom tree gently dropping petals.",
        isWalkable = false
    ),
    LAMP_POST(
        title = "Street Lantern",
        cost = 30,
        description = "Wrought iron streetlamp glowing warm amber at dusk and night.",
        isWalkable = false
    ),
    PARK_BENCH(
        title = "Park Bench",
        cost = 35,
        description = "Cozy wooden seat for tired walkers to sip tea and read.",
        isWalkable = true
    )
}

data class TownTile(
    val x: Int,
    val y: Int,
    val terrain: TerrainType = TerrainType.MEADOW,
    val building: BuildingType? = null,
    val level: Int = 1
) {
    val totalVibesPerSec: Double
        get() = building?.vibesPerSecForLevel(level) ?: 0.0

    val isOccupied: Boolean
        get() = building != null || terrain == TerrainType.SAKURA_TREE || terrain == TerrainType.LAMP_POST
}
