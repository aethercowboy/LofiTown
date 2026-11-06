package com.example.lofitown.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LofiCoral
import com.example.ui.theme.LofiLavender
import com.example.ui.theme.LofiMatcha
import com.example.ui.theme.LofiPeach
import com.example.ui.theme.LofiSkyBlue
import com.example.ui.theme.LofiYellowGlow

enum class BuildingCategory(val title: String) {
    BEVERAGE_FOOD("Cafe & Bites"),
    MUSIC_CULTURE("Art & Tunes"),
    NATURE_ZEN("Greenery & Zen"),
    SANCTUARY("Cozy Havens")
}

enum class BuildingType(
    val id: String,
    val displayName: String,
    val category: BuildingCategory,
    val cost: Long,
    val vibesPerSec: Double,
    val accentColor: Color,
    val description: String,
    val ambientQuote: String
) {
    COFFEE_SHOP(
        id = "coffee_shop",
        displayName = "Lofi Roastery",
        category = BuildingCategory.BEVERAGE_FOOD,
        cost = 15,
        vibesPerSec = 1.0,
        accentColor = LofiPeach,
        description = "A warm brick corner cafe with steaming mugs and soft acoustic chords.",
        ambientQuote = "Fresh pour-over drippings rhythmically into ceramic cups."
    ),
    VINYL_SHOP(
        id = "vinyl_shop",
        displayName = "Vinyl & Tapes",
        category = BuildingCategory.MUSIC_CULTURE,
        cost = 55,
        vibesPerSec = 3.2,
        accentColor = LofiLavender,
        description = "Turntables spinning dusty 70s Japanese jazz fusion records.",
        ambientQuote = "Gentle crackles before the mellow piano groove kicks in."
    ),
    BOOKSTORE(
        id = "bookstore",
        displayName = "Rainy Bookstore",
        category = BuildingCategory.MUSIC_CULTURE,
        cost = 160,
        vibesPerSec = 8.5,
        accentColor = LofiYellowGlow,
        description = "Floor-to-ceiling vintage novels, cozy reading nooks, and sleeping cats.",
        ambientQuote = "Pages turning softly while rain taps the window pane."
    ),
    RAMEN_STALL(
        id = "ramen_stall",
        displayName = "Midnight Ramen",
        category = BuildingCategory.BEVERAGE_FOOD,
        cost = 450,
        vibesPerSec = 22.0,
        accentColor = LofiCoral,
        description = "Warm paper lanterns glowing in the evening mist with rich miso broth.",
        ambientQuote = "Steamy noodles slurped slowly in the quiet alley."
    ),
    BOTANICAL_GREENHOUSE(
        id = "greenhouse",
        displayName = "Glass Greenhouse",
        category = BuildingCategory.NATURE_ZEN,
        cost = 1200,
        vibesPerSec = 58.0,
        accentColor = LofiMatcha,
        description = "Glass conservatory filled with monsteras, ferns, and terrarium moss.",
        ambientQuote = "Drops of condensation trickling down peaceful glass panes."
    ),
    CAT_CAFE(
        id = "cat_cafe",
        displayName = "Mochi's Cat Cafe",
        category = BuildingCategory.SANCTUARY,
        cost = 3200,
        vibesPerSec = 140.0,
        accentColor = LofiPeach,
        description = "Fluffy calico cats curling up in wicker baskets and soft sunny rugs.",
        ambientQuote = "Purring harmonies vibrating at a comforting 25 Hertz."
    ),
    BAKERY(
        id = "bakery",
        displayName = "Pastel Bakery",
        category = BuildingCategory.BEVERAGE_FOOD,
        cost = 8000,
        vibesPerSec = 340.0,
        accentColor = LofiYellowGlow,
        description = "Warm sourdough loaves, strawberry milk buns, and cardamom twists.",
        ambientQuote = "Sweet vanilla and toasted sugar wafting into the dusk air."
    ),
    RADIO_BEACON(
        id = "radio_beacon",
        displayName = "Lo-Fi FM Beacon",
        category = BuildingCategory.MUSIC_CULTURE,
        cost = 20000,
        vibesPerSec = 820.0,
        accentColor = LofiSkyBlue,
        description = "A nostalgic radio transmitter antenna sending chill beats across the clouds.",
        ambientQuote = "Pulsing comforting waves through old boombox speakers."
    ),
    OBSERVATORY(
        id = "observatory",
        displayName = "Starlight Dome",
        category = BuildingCategory.SANCTUARY,
        cost = 50000,
        vibesPerSec = 2100.0,
        accentColor = LofiLavender,
        description = "Polished brass telescope trained on the rings of distant worlds.",
        ambientQuote = "Whispers of distant comets gliding across midnight indigo."
    ),
    ZEN_GARDEN(
        id = "zen_garden",
        displayName = "Bonsai Koi Pond",
        category = BuildingCategory.NATURE_ZEN,
        cost = 120000,
        vibesPerSec = 5200.0,
        accentColor = LofiMatcha,
        description = "Raked white gravel, moss-covered boulders, and lazy orange koi.",
        ambientQuote = "A bamboo shishi-odoshi water dropper clacking every minute."
    );

    fun upgradeCost(currentLevel: Int): Long {
        return (cost * Math.pow(1.6, currentLevel.toDouble())).toLong()
    }

    fun vibesPerSecForLevel(currentLevel: Int): Double {
        return vibesPerSec * (1.0 + (currentLevel - 1) * 0.75)
    }
}
