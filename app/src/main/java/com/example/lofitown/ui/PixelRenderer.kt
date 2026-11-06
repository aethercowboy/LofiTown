package com.example.lofitown.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.lofitown.model.BuildingType
import com.example.lofitown.model.Citizen
import com.example.lofitown.model.TerrainType
import com.example.lofitown.model.TimeOfDay
import com.example.lofitown.model.TownTile
import com.example.lofitown.model.Weather
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object PixelRenderer {
    const val TILE_WIDTH = 96f
    const val TILE_HEIGHT = 48f
    const val TILE_DEPTH = 18f

    fun gridToScreen(gridX: Float, gridY: Float, originX: Float, originY: Float): Offset {
        val screenX = originX + (gridX - gridY) * (TILE_WIDTH / 2f)
        val screenY = originY + (gridX + gridY) * (TILE_HEIGHT / 2f)
        return Offset(screenX, screenY)
    }

    fun screenToGrid(screenX: Float, screenY: Float, originX: Float, originY: Float): Pair<Int, Int> {
        val dx = screenX - originX
        val dy = screenY - originY
        val halfW = TILE_WIDTH / 2f
        val halfH = TILE_HEIGHT / 2f

        val gridX = ((dx / halfW) + (dy / halfH)) / 2f
        val gridY = ((dy / halfH) - (dx / halfW)) / 2f

        return Pair(kotlin.math.floor(gridX).toInt(), kotlin.math.floor(gridY).toInt())
    }

    fun drawTownScene(
        scope: DrawScope,
        tiles: List<TownTile>,
        citizens: List<Citizen>,
        selectedTile: Pair<Int, Int>?,
        timeOfDay: TimeOfDay,
        weather: Weather,
        animTime: Float, // seconds continuously increasing
        originX: Float,
        originY: Float
    ) {
        val tilesSorted = tiles.sortedWith(compareBy({ it.x + it.y }, { it.x }))

        // 1. Draw base terrain tiles
        for (tile in tilesSorted) {
            val center = gridToScreen(tile.x.toFloat(), tile.y.toFloat(), originX, originY)
            drawBaseTile(scope, tile, center, timeOfDay, animTime)
        }

        // 2. Draw streetlamp ground glow under buildings/night
        if (timeOfDay.lampGlow > 0.1f) {
            for (tile in tiles) {
                if (tile.terrain == TerrainType.LAMP_POST) {
                    val center = gridToScreen(tile.x.toFloat(), tile.y.toFloat(), originX, originY)
                    scope.drawCircle(
                        color = Color(0xFFFFE082).copy(alpha = 0.28f * timeOfDay.lampGlow),
                        radius = TILE_WIDTH * 0.75f,
                        center = center
                    )
                }
            }
        }

        // 3. Draw Buildings, Decor, and Citizens in depth sorted order
        for (tile in tilesSorted) {
            val center = gridToScreen(tile.x.toFloat(), tile.y.toFloat(), originX, originY)

            // Draw decor items on terrain
            when (tile.terrain) {
                TerrainType.SAKURA_TREE -> drawSakuraTree(scope, center, animTime, timeOfDay)
                TerrainType.LAMP_POST -> drawLampPost(scope, center, timeOfDay, animTime)
                TerrainType.PARK_BENCH -> drawParkBench(scope, center)
                TerrainType.BRIDGE -> drawBridgeRailing(scope, center)
                else -> {}
            }

            // Draw building
            tile.building?.let { b ->
                drawBuilding(scope, b, tile.level, center, timeOfDay, animTime)
            }

            // Draw citizens whose position is close to this tile depth
            val citizensOnDepth = citizens.filter {
                val depth = it.gridX + it.gridY
                depth >= (tile.x + tile.y) - 0.5f && depth < (tile.x + tile.y) + 0.5f
            }
            for (citizen in citizensOnDepth) {
                val citizenPos = gridToScreen(citizen.gridX, citizen.gridY, originX, originY)
                drawCitizen(scope, citizen, citizenPos, weather, animTime)
            }

            // Highlight selected tile
            if (selectedTile?.first == tile.x && selectedTile?.second == tile.y) {
                drawSelectionBracket(scope, center, animTime)
            }
        }

        // 4. Draw Weather Overlays (Rain streaks or Sakura petals)
        when (weather) {
            Weather.GENTLE_RAIN -> drawRainOverlay(scope, animTime)
            Weather.SAKURA_DRIFT -> drawSakuraOverlay(scope, animTime)
            Weather.COZY_MIST -> drawMistOverlay(scope, animTime)
            Weather.CLEAR -> {}
        }

        // 5. Ambient Lighting Overlay
        if (timeOfDay.ambientFilter.alpha > 0.01f) {
            scope.drawRect(
                color = timeOfDay.ambientFilter,
                size = scope.size
            )
        }
    }

    private fun drawBaseTile(
        scope: DrawScope,
        tile: TownTile,
        center: Offset,
        timeOfDay: TimeOfDay,
        animTime: Float
    ) {
        val halfW = TILE_WIDTH / 2f
        val halfH = TILE_HEIGHT / 2f

        val topPoint = Offset(center.x, center.y - halfH)
        val rightPoint = Offset(center.x + halfW, center.y)
        val bottomPoint = Offset(center.x, center.y + halfH)
        val leftPoint = Offset(center.x - halfW, center.y)

        // 3D side drop (left and right facets)
        val leftDrop = Path().apply {
            moveTo(leftPoint.x, leftPoint.y)
            lineTo(bottomPoint.x, bottomPoint.y)
            lineTo(bottomPoint.x, bottomPoint.y + TILE_DEPTH)
            lineTo(leftPoint.x, leftPoint.y + TILE_DEPTH)
            close()
        }
        val rightDrop = Path().apply {
            moveTo(bottomPoint.x, bottomPoint.y)
            lineTo(rightPoint.x, rightPoint.y)
            lineTo(rightPoint.x, rightPoint.y + TILE_DEPTH)
            lineTo(bottomPoint.x, bottomPoint.y + TILE_DEPTH)
            close()
        }

        val sideColorLeft = when (tile.terrain) {
            TerrainType.RIVER -> Color(0xFF2C5E6E)
            TerrainType.COBBLESTONE -> Color(0xFF5A524A)
            else -> Color(0xFF3E5A35)
        }
        val sideColorRight = when (tile.terrain) {
            TerrainType.RIVER -> Color(0xFF1B3D47)
            TerrainType.COBBLESTONE -> Color(0xFF423B35)
            else -> Color(0xFF2A3F24)
        }

        scope.drawPath(leftDrop, sideColorLeft)
        scope.drawPath(rightDrop, sideColorRight)

        // Top diamond facet
        val topFacet = Path().apply {
            moveTo(topPoint.x, topPoint.y)
            lineTo(rightPoint.x, rightPoint.y)
            lineTo(bottomPoint.x, bottomPoint.y)
            lineTo(leftPoint.x, leftPoint.y)
            close()
        }

        val baseColor = when (tile.terrain) {
            TerrainType.MEADOW, TerrainType.SAKURA_TREE, TerrainType.LAMP_POST, TerrainType.PARK_BENCH ->
                if ((tile.x + tile.y) % 2 == 0) Color(0xFF679B5E) else Color(0xFF5C8E54)
            TerrainType.COBBLESTONE ->
                if ((tile.x + tile.y) % 2 == 0) Color(0xFF91867A) else Color(0xFF82786D)
            TerrainType.RIVER -> {
                // Shimmering river with animated sine color
                val shimmer = sin(animTime * 2f + tile.x * 0.8f + tile.y * 1.2f) * 0.06f
                Color(0xFF3F8294).copy(
                    red = (0.25f + shimmer).coerceIn(0.1f, 0.4f),
                    blue = (0.58f + shimmer).coerceIn(0.4f, 0.8f)
                )
            }
            TerrainType.BRIDGE -> Color(0xFF8A5A38)
        }

        scope.drawPath(topFacet, baseColor)

        // Subtle tile outline in pixel style
        scope.drawPath(topFacet, Color(0x22000000), style = Stroke(width = 1.2f))

        // Surface texture accents
        when (tile.terrain) {
            TerrainType.COBBLESTONE -> {
                // Little cobblestone dots
                scope.drawCircle(Color(0xFF6B6258), 2.5f, Offset(center.x - 12f, center.y - 4f))
                scope.drawCircle(Color(0xFFA59A8E), 2.5f, Offset(center.x + 8f, center.y - 6f))
                scope.drawCircle(Color(0xFF6B6258), 2.5f, Offset(center.x - 2f, center.y + 6f))
                scope.drawCircle(Color(0xFFA59A8E), 2.5f, Offset(center.x + 14f, center.y + 2f))
            }
            TerrainType.MEADOW -> {
                // Tiny grass tufts & wildflowers
                scope.drawCircle(Color(0xFF7CB872), 2f, Offset(center.x - 14f, center.y))
                scope.drawCircle(Color(0xFF7CB872), 2f, Offset(center.x - 12f, center.y - 3f))
                if ((tile.x * 3 + tile.y * 7) % 3 == 0) {
                    // Cute pastel flower
                    scope.drawCircle(Color(0xFFFFD166), 2.2f, Offset(center.x + 10f, center.y - 2f))
                }
            }
            TerrainType.RIVER -> {
                // Specular water ripples
                val rippleShift = (animTime * 15f + tile.x * 12f) % TILE_WIDTH - (TILE_WIDTH / 2f)
                scope.drawLine(
                    color = Color(0x99D2F3F9),
                    start = Offset(center.x + rippleShift - 8f, center.y - 2f),
                    end = Offset(center.x + rippleShift + 8f, center.y - 2f),
                    strokeWidth = 1.8f
                )
            }
            TerrainType.BRIDGE -> {
                // Wooden bridge plank lines
                scope.drawLine(
                    color = Color(0xFF663E22),
                    start = Offset(center.x - halfW * 0.6f, center.y - halfH * 0.3f),
                    end = Offset(center.x + halfW * 0.6f, center.y + halfH * 0.3f),
                    strokeWidth = 2f
                )
                scope.drawLine(
                    color = Color(0xFF663E22),
                    start = Offset(center.x - halfW * 0.3f, center.y - halfH * 0.6f),
                    end = Offset(center.x + halfW * 0.3f, center.y + halfH * 0.6f),
                    strokeWidth = 2f
                )
            }
            else -> {}
        }
    }

    private fun drawBuilding(
        scope: DrawScope,
        building: BuildingType,
        level: Int,
        center: Offset,
        timeOfDay: TimeOfDay,
        animTime: Float
    ) {
        val windowColor = if (timeOfDay.windowGlow > 0.2f) {
            Color(0xFFFFE082).copy(alpha = (0.7f + 0.3f * timeOfDay.windowGlow))
        } else {
            Color(0xFF8ECAE6)
        }

        when (building) {
            BuildingType.COFFEE_SHOP -> {
                // 1. Cozy Brick & Timber Cafe
                val w = 48f
                val h = 42f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                // Building base wall
                scope.drawRect(Color(0xFF8F4C38), Offset(bLeft, bTop + 14f), Size(w, h - 14f))

                // Roof slant
                val roof = Path().apply {
                    moveTo(bLeft - 6f, bTop + 14f)
                    lineTo(center.x, bTop - 4f)
                    lineTo(bLeft + w + 6f, bTop + 14f)
                    close()
                }
                scope.drawPath(roof, Color(0xFFC75D43))

                // Striped awning
                scope.drawRect(Color(0xFFF7EBE1), Offset(bLeft + 6f, bTop + 24f), Size(w - 12f, 7f))
                scope.drawRect(Color(0xFFC75D43), Offset(bLeft + 10f, bTop + 24f), Size(6f, 7f))
                scope.drawRect(Color(0xFFC75D43), Offset(bLeft + 24f, bTop + 24f), Size(6f, 7f))

                // Door & glowing window
                scope.drawRect(Color(0xFF4A2B1D), Offset(bLeft + 8f, bTop + 31f), Size(11f, 15f))
                scope.drawRect(windowColor, Offset(bLeft + 24f, bTop + 32f), Size(16f, 11f))
                scope.drawRect(Color(0xFF3B1E12), Offset(bLeft + 31f, bTop + 32f), Size(2f, 11f)) // frame

                // Chimney & animated smoke
                val chimLeft = bLeft + 8f
                val chimTop = bTop - 2f
                scope.drawRect(Color(0xFF5A2F22), Offset(chimLeft, chimTop), Size(8f, 10f))
                // Animated pixel smoke puffs
                for (p in 0..2) {
                    val pAge = (animTime * 1.5f + p * 0.7f) % 2f
                    val puffY = chimTop - (pAge * 18f)
                    val puffX = chimLeft + 4f + sin(pAge * 3f) * 4f
                    val puffAlpha = (1f - (pAge / 2f)).coerceIn(0f, 0.7f)
                    scope.drawCircle(Color(0xFFEDE8E3).copy(alpha = puffAlpha), 3f + pAge * 2f, Offset(puffX, puffY))
                }

                // Tiny coffee cup rooftop sign
                scope.drawCircle(Color(0xFFF7EBE1), 5f, Offset(center.x, bTop + 4f))
                scope.drawCircle(Color(0xFF8F4C38), 3f, Offset(center.x, bTop + 4f))
            }

            BuildingType.VINYL_SHOP -> {
                // 2. Vinyl & Tapes Lounge
                val w = 50f
                val h = 38f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                // Retro purple wall
                scope.drawRect(Color(0xFF382947), Offset(bLeft, bTop + 10f), Size(w, h - 10f))
                // Flat parapet roof with neon trim
                scope.drawRect(Color(0xFF523D66), Offset(bLeft - 4f, bTop + 6f), Size(w + 8f, 8f))
                scope.drawRect(Color(0xFFCDB4DB), Offset(bLeft - 4f, bTop + 6f), Size(w + 8f, 2f))

                // Display window showing vinyl records
                scope.drawRect(windowColor, Offset(bLeft + 6f, bTop + 18f), Size(24f, 18f))
                // Tiny album sleeve inside
                scope.drawRect(Color(0xFFF28482), Offset(bLeft + 10f, bTop + 22f), Size(8f, 8f))

                // Modern glass door
                scope.drawRect(Color(0xFF241A30), Offset(bLeft + 33f, bTop + 18f), Size(12f, 22f))
                scope.drawCircle(Color(0xFFFFD166), 1.5f, Offset(bLeft + 35f, bTop + 29f))

                // Rooftop spinning vinyl record
                val vinylRot = animTime * 3f
                val vCenter = Offset(center.x, bTop - 4f)
                scope.drawCircle(Color(0xFF141218), 9f, vCenter)
                scope.drawCircle(Color(0xFFCDB4DB), 3.5f, vCenter)
                scope.drawCircle(Color(0xFF141218), 1f, vCenter)

                // Floating musical note
                val noteY = vCenter.y - 12f - (sin(animTime * 2.5f) * 4f)
                scope.drawCircle(Color(0xFFCDB4DB), 2.2f, Offset(vCenter.x + 8f, noteY))
            }

            BuildingType.BOOKSTORE -> {
                // 3. Rainy Bookstore with Cat
                val w = 52f
                val h = 46f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                // Dark mahogany building
                scope.drawRect(Color(0xFF4A3222), Offset(bLeft, bTop + 12f), Size(w, h - 12f))
                // Emerald pitched roof
                val roof = Path().apply {
                    moveTo(bLeft - 4f, bTop + 12f)
                    lineTo(center.x, bTop - 4f)
                    lineTo(bLeft + w + 4f, bTop + 12f)
                    close()
                }
                scope.drawPath(roof, Color(0xFF2C5E43))

                // Dual arched warm windows with book silhouette
                scope.drawRect(windowColor, Offset(bLeft + 6f, bTop + 18f), Size(16f, 20f))
                scope.drawRect(windowColor, Offset(bLeft + 30f, bTop + 18f), Size(16f, 20f))
                // Bookshelves
                scope.drawLine(Color(0xFF332014), Offset(bLeft + 6f, bTop + 28f), Offset(bLeft + 22f, bTop + 28f), 2f)
                scope.drawLine(Color(0xFF332014), Offset(bLeft + 30f, bTop + 28f), Offset(bLeft + 46f, bTop + 28f), 2f)

                // Sleeping cat curled on canopy
                scope.drawCircle(Color(0xFFFFA559), 3f, Offset(bLeft + 12f, bTop + 11f))
            }

            BuildingType.RAMEN_STALL -> {
                // 4. Midnight Ramen Stall
                val w = 46f
                val h = 34f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                // Timber counter
                scope.drawRect(Color(0xFF5A3C26), Offset(bLeft + 4f, bTop + 16f), Size(w - 8f, h - 16f))
                // Overhanging cedar roof
                val roof = Path().apply {
                    moveTo(bLeft - 4f, bTop + 16f)
                    lineTo(center.x, bTop + 4f)
                    lineTo(bLeft + w + 4f, bTop + 16f)
                    close()
                }
                scope.drawPath(roof, Color(0xFF382315))

                // Glowing red paper lanterns (chochin)
                val lanternGlow = if (timeOfDay.lampGlow > 0.2f) 1f else 0.7f
                scope.drawCircle(Color(0xFFE63946).copy(alpha = lanternGlow), 4.5f, Offset(bLeft + 6f, bTop + 18f))
                scope.drawCircle(Color(0xFFE63946).copy(alpha = lanternGlow), 4.5f, Offset(bLeft + w - 6f, bTop + 18f))

                // Counter stools
                scope.drawCircle(Color(0xFF8A5A38), 3.5f, Offset(bLeft + 14f, bBottom - 2f))
                scope.drawCircle(Color(0xFF8A5A38), 3.5f, Offset(bLeft + 32f, bBottom - 2f))

                // Steaming ramen bowl on counter
                scope.drawArc(Color(0xFFF7EBE1), 0f, 180f, true, Offset(center.x - 5f, bTop + 16f), Size(10f, 6f))
                // Steam line
                val steamY = bTop + 12f - (sin(animTime * 3f) * 3f)
                scope.drawLine(Color(0xAAF7EBE1), Offset(center.x, bTop + 15f), Offset(center.x, steamY), 1.5f)
            }

            BuildingType.BOTANICAL_GREENHOUSE -> {
                // 5. Glass Conservatory & Plants
                val w = 48f
                val h = 38f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                // Translucent glass body
                scope.drawRect(Color(0x99A3C9A8), Offset(bLeft, bTop + 10f), Size(w, h - 10f))
                // Brass/iron frames
                scope.drawRect(Color(0xFF4A6B53), Offset(bLeft, bTop + 10f), Size(w, h - 10f), style = Stroke(2f))
                scope.drawLine(Color(0xFF4A6B53), Offset(center.x, bTop + 10f), Offset(center.x, bBottom), 1.5f)

                // Glass pitched roof
                val roof = Path().apply {
                    moveTo(bLeft - 2f, bTop + 10f)
                    lineTo(center.x, bTop)
                    lineTo(bLeft + w + 2f, bTop + 10f)
                    close()
                }
                scope.drawPath(roof, Color(0xBBA3C9A8))
                scope.drawPath(roof, Color(0xFF4A6B53), style = Stroke(1.5f))

                // Fern silhouettes visible inside
                scope.drawCircle(Color(0xFF2D5A38), 5f, Offset(bLeft + 12f, bTop + 24f))
                scope.drawCircle(Color(0xFF2D5A38), 6f, Offset(bLeft + 34f, bTop + 26f))
            }

            BuildingType.CAT_CAFE -> {
                // 6. Mochi's Cat Cafe
                val w = 48f
                val h = 40f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                scope.drawRect(Color(0xFFF2D1C9), Offset(bLeft, bTop + 14f), Size(w, h - 14f))

                // Roof with cat ear ornaments
                val roof = Path().apply {
                    moveTo(bLeft - 4f, bTop + 14f)
                    lineTo(bLeft + 8f, bTop - 4f) // Left cat ear
                    lineTo(bLeft + 14f, bTop + 2f)
                    lineTo(center.x, bTop)
                    lineTo(bLeft + w - 14f, bTop + 2f)
                    lineTo(bLeft + w - 8f, bTop - 4f) // Right cat ear
                    lineTo(bLeft + w + 4f, bTop + 14f)
                    close()
                }
                scope.drawPath(roof, Color(0xFFE5A88B))

                // Paw print window
                scope.drawCircle(windowColor, 5f, Offset(center.x, bTop + 24f))
                scope.drawCircle(windowColor, 2f, Offset(center.x - 4f, bTop + 18f))
                scope.drawCircle(windowColor, 2f, Offset(center.x, bTop + 16f))
                scope.drawCircle(windowColor, 2f, Offset(center.x + 4f, bTop + 18f))

                // Tiny sleeping cat on porch
                scope.drawCircle(Color(0xFFFAF0E6), 3.5f, Offset(bLeft + 10f, bBottom - 3f))
            }

            BuildingType.BAKERY -> {
                // 7. Pastel Bakery
                val w = 48f
                val h = 42f
                val bLeft = center.x - w / 2f
                val bBottom = center.y + 4f
                val bTop = bBottom - h

                scope.drawRect(Color(0xFFF9F0DB), Offset(bLeft, bTop + 14f), Size(w, h - 14f))
                // Striped pink roof
                val roof = Path().apply {
                    moveTo(bLeft - 4f, bTop + 14f)
                    lineTo(center.x, bTop - 2f)
                    lineTo(bLeft + w + 4f, bTop + 14f)
                    close()
                }
                scope.drawPath(roof, Color(0xFFF28482))

                // Croissant / Pretzel sign
                scope.drawCircle(Color(0xFFE59866), 4.5f, Offset(center.x, bTop + 6f))

                // Glass pastry showcase
                scope.drawRect(windowColor, Offset(bLeft + 6f, bTop + 22f), Size(22f, 16f))
                scope.drawCircle(Color(0xFFD48B47), 2f, Offset(bLeft + 12f, bTop + 30f)) // bun
                scope.drawCircle(Color(0xFFD48B47), 2f, Offset(bLeft + 18f, bTop + 30f)) // bun
            }

            BuildingType.RADIO_BEACON -> {
                // 8. Lo-Fi FM Radio Beacon
                val bBottom = center.y + 4f
                val towerH = 46f
                val towerTop = bBottom - towerH

                // Lattice antenna structure
                scope.drawLine(Color(0xFFC75D43), Offset(center.x - 12f, bBottom), Offset(center.x, towerTop), 2.5f)
                scope.drawLine(Color(0xFFC75D43), Offset(center.x + 12f, bBottom), Offset(center.x, towerTop), 2.5f)
                scope.drawLine(Color(0xFFEDE8E3), Offset(center.x - 8f, bBottom - 16f), Offset(center.x + 8f, bBottom - 16f), 2f)
                scope.drawLine(Color(0xFFEDE8E3), Offset(center.x - 4f, bBottom - 32f), Offset(center.x + 4f, bBottom - 32f), 2f)

                // Blinking red beacon light
                val blink = if ((animTime * 2f).toInt() % 2 == 0) Color(0xFFFF3333) else Color(0x66FF3333)
                scope.drawCircle(blink, 4f, Offset(center.x, towerTop))

                // Pulsing radio wave arc
                val waveAlpha = (1f - ((animTime * 1.5f) % 1f)).coerceIn(0f, 0.8f)
                scope.drawArc(
                    color = Color(0xFF8ECAE6).copy(alpha = waveAlpha),
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(center.x - 14f, towerTop - 12f),
                    size = Size(28f, 24f),
                    style = Stroke(1.5f)
                )
            }

            BuildingType.OBSERVATORY -> {
                // 9. Starlight Observatory Dome
                val bBottom = center.y + 4f
                val domeR = 18f
                val domeCenter = Offset(center.x, bBottom - 14f)

                // Circular white stucco base
                scope.drawRect(Color(0xFFEDE8E3), Offset(center.x - domeR, bBottom - 14f), Size(domeR * 2f, 14f))
                // Dome arch
                scope.drawArc(Color(0xFF382947), 180f, 180f, true, Offset(center.x - domeR, bBottom - 14f - domeR), Size(domeR * 2f, domeR * 2f))

                // Protruding brass telescope
                val teleStart = Offset(center.x + 2f, bBottom - 20f)
                val teleEnd = Offset(center.x + 16f, bBottom - 34f)
                scope.drawLine(Color(0xFFFFD166), teleStart, teleEnd, 4f)
                scope.drawCircle(Color(0xFF8ECAE6), 2.5f, teleEnd)
            }

            BuildingType.ZEN_GARDEN -> {
                // 10. Bonsai & Koi Pond
                val bBottom = center.y + 2f
                // Miniature koi pond
                scope.drawOval(Color(0xFF3F8294), Offset(center.x - 16f, bBottom - 14f), Size(32f, 18f))

                // Tiny swimming orange koi
                val koiAngle = animTime * 2f
                val koiX = center.x + cos(koiAngle) * 9f
                val koiY = bBottom - 5f + sin(koiAngle) * 4f
                scope.drawCircle(Color(0xFFFFA559), 2.5f, Offset(koiX, koiY))

                // Bonsai on rock
                scope.drawCircle(Color(0xFF5A4A3A), 5f, Offset(center.x - 10f, bBottom - 10f))
                scope.drawCircle(Color(0xFF2D5A38), 6f, Offset(center.x - 12f, bBottom - 18f))
            }
        }

        // Level badge for upgraded buildings (> 1)
        if (level > 1) {
            val badgeX = center.x + 18f
            val badgeY = center.y - 24f
            scope.drawCircle(Color(0xFFFFD166), 6f, Offset(badgeX, badgeY))
            scope.drawCircle(Color(0xFF282033), 4f, Offset(badgeX, badgeY))
        }
    }

    private fun drawSakuraTree(scope: DrawScope, center: Offset, animTime: Float, timeOfDay: TimeOfDay) {
        val trunkBase = Offset(center.x, center.y + 2f)
        val trunkTop = Offset(center.x, center.y - 18f)

        // Gnarled trunk
        scope.drawLine(Color(0xFF5A3C26), trunkBase, trunkTop, 5f)
        scope.drawLine(Color(0xFF5A3C26), trunkTop, Offset(center.x - 8f, center.y - 26f), 3.5f)
        scope.drawLine(Color(0xFF5A3C26), trunkTop, Offset(center.x + 8f, center.y - 26f), 3.5f)

        // Billowing pink foliage clusters (with subtle swaying)
        val sway = sin(animTime * 1.8f) * 2f
        val folCenter = Offset(center.x + sway, center.y - 30f)
        scope.drawCircle(Color(0xFFF4A7B9), 16f, folCenter)
        scope.drawCircle(Color(0xFFFFCCD5), 11f, Offset(folCenter.x - 6f, folCenter.y - 4f))
        scope.drawCircle(Color(0xFFE28099), 10f, Offset(folCenter.x + 6f, folCenter.y + 4f))

        // Tiny falling blossom petal
        val petalY = center.y - 20f + ((animTime * 12f) % 24f)
        val petalX = center.x - 12f + sin(animTime * 2.5f) * 6f
        scope.drawCircle(Color(0xFFFFB7B2), 1.8f, Offset(petalX, petalY))
    }

    private fun drawLampPost(scope: DrawScope, center: Offset, timeOfDay: TimeOfDay, animTime: Float) {
        val base = Offset(center.x, center.y + 2f)
        val top = Offset(center.x, center.y - 24f)

        // Wrought iron pole
        scope.drawLine(Color(0xFF24222B), base, top, 2.5f)

        // Lantern head
        val lanternCenter = Offset(center.x, top.y - 4f)
        val glowColor = if (timeOfDay.lampGlow > 0.1f) {
            Color(0xFFFFD166)
        } else {
            Color(0xFF888492)
        }
        scope.drawRect(Color(0xFF24222B), Offset(lanternCenter.x - 4f, lanternCenter.y - 6f), Size(8f, 10f), style = Stroke(1.5f))
        scope.drawCircle(glowColor, 3f, lanternCenter)

        // Soft radial aura at dusk/night
        if (timeOfDay.lampGlow > 0.1f) {
            val auraR = 14f + sin(animTime * 3f) * 1.5f
            scope.drawCircle(Color(0xFFFFE082).copy(alpha = 0.35f * timeOfDay.lampGlow), auraR, lanternCenter)
        }
    }

    private fun drawParkBench(scope: DrawScope, center: Offset) {
        val bX = center.x - 12f
        val bY = center.y - 6f
        scope.drawRect(Color(0xFF6E472D), Offset(bX, bY), Size(24f, 6f)) // Seat
        scope.drawRect(Color(0xFF54341E), Offset(bX, bY - 6f), Size(24f, 4f)) // Backrest
        // Iron legs
        scope.drawLine(Color(0xFF24222B), Offset(bX + 2f, bY + 6f), Offset(bX + 2f, bY + 10f), 2f)
        scope.drawLine(Color(0xFF24222B), Offset(bX + 22f, bY + 6f), Offset(bX + 22f, bY + 10f), 2f)
    }

    private fun drawBridgeRailing(scope: DrawScope, center: Offset) {
        val halfW = TILE_WIDTH / 2f
        val halfH = TILE_HEIGHT / 2f
        // Wooden handrails on bridge sides
        scope.drawLine(Color(0xFF663E22), Offset(center.x - halfW * 0.5f, center.y - 10f), Offset(center.x + halfW * 0.5f, center.y + halfH * 0.5f - 10f), 2f)
    }

    private fun drawCitizen(scope: DrawScope, citizen: Citizen, pos: Offset, weather: Weather, animTime: Float) {
        val walkCycle = ((animTime * 5f).toInt() % 2) * 2f - 1f

        if (citizen.isCat) {
            // Fluffy cat sprite (napping or walking)
            val catY = pos.y - 6f
            scope.drawOval(Color(0xFFFAF0E6), Offset(pos.x - 6f, catY), Size(12f, 7f)) // body
            scope.drawCircle(Color(0xFFFAF0E6), 3.5f, Offset(pos.x + 4f, catY - 2f)) // head
            // Orange calico patch
            scope.drawCircle(Color(0xFFFFA559), 2f, Offset(pos.x - 2f, catY + 1f))
            // Cat ears
            scope.drawCircle(Color(0xFFFFA559), 1.2f, Offset(pos.x + 3f, catY - 5f))
            scope.drawCircle(Color(0xFFFAF0E6), 1.2f, Offset(pos.x + 6f, catY - 5f))
            // Tail with slow swish
            val tailSwish = sin(animTime * 3f) * 3f
            scope.drawLine(Color(0xFFFAF0E6), Offset(pos.x - 5f, catY + 2f), Offset(pos.x - 10f, catY - 1f + tailSwish), 1.5f)
            return
        }

        val footY = pos.y
        val bodyY = pos.y - 12f
        val headY = pos.y - 20f

        // Legs (animated walking)
        scope.drawLine(Color(0xFF2B2033), Offset(pos.x - 2f, bodyY + 6f), Offset(pos.x - 2f + walkCycle, footY), 2f)
        scope.drawLine(Color(0xFF2B2033), Offset(pos.x + 2f, bodyY + 6f), Offset(pos.x + 2f - walkCycle, footY), 2f)

        // Sweater / Clothes
        scope.drawRect(citizen.clothesColor, Offset(pos.x - 5f, bodyY), Size(10f, 8f))

        // Head & Hair
        scope.drawCircle(Color(0xFFFCD5B5), 4f, Offset(pos.x, headY))
        scope.drawCircle(citizen.hairColor, 4f, Offset(pos.x, headY - 1.5f))

        // Cute Headphones (for Maya / Sam)
        if (citizen.name == "Maya" || citizen.name == "Sam") {
            scope.drawArc(Color(0xFFE5A88B), 180f, 180f, false, Offset(pos.x - 5f, headY - 6f), Size(10f, 8f), style = Stroke(1.5f))
            scope.drawCircle(Color(0xFFE5A88B), 1.8f, Offset(pos.x - 4f, headY))
            scope.drawCircle(Color(0xFFE5A88B), 1.8f, Offset(pos.x + 4f, headY))
        }

        // Umbrella if raining
        if (weather == Weather.GENTLE_RAIN) {
            val umbCenter = Offset(pos.x, headY - 8f)
            scope.drawArc(Color(0xFF8ECAE6), 180f, 180f, true, Offset(umbCenter.x - 10f, umbCenter.y - 8f), Size(20f, 12f))
            scope.drawLine(Color(0xFF2B2033), umbCenter, Offset(pos.x + 2f, bodyY), 1.5f)
        }
    }

    private fun drawSelectionBracket(scope: DrawScope, center: Offset, animTime: Float) {
        val halfW = TILE_WIDTH / 2f
        val halfH = TILE_HEIGHT / 2f
        val pulse = 1f + sin(animTime * 4f) * 0.08f
        val w = halfW * pulse
        val h = halfH * pulse

        val bracketColor = Color(0xFFFFD166)
        val bracketLen = 8f

        // 4 corners of isometric diamond
        val top = Offset(center.x, center.y - h)
        val right = Offset(center.x + w, center.y)
        val bottom = Offset(center.x, center.y + h)
        val left = Offset(center.x - w, center.y)

        scope.drawCircle(bracketColor, 3f, top)
        scope.drawCircle(bracketColor, 3f, right)
        scope.drawCircle(bracketColor, 3f, bottom)
        scope.drawCircle(bracketColor, 3f, left)
    }

    private fun drawRainOverlay(scope: DrawScope, animTime: Float) {
        val rainColor = Color(0x66B0E0E6)
        val rainCount = 45
        for (i in 0 until rainCount) {
            val startX = ((i * 37) % scope.size.width.toInt()).toFloat()
            val speed = 350f + (i % 5) * 40f
            val yOffset = ((animTime * speed) + i * 53) % scope.size.height
            scope.drawLine(
                color = rainColor,
                start = Offset(startX, yOffset),
                end = Offset(startX - 6f, yOffset + 14f),
                strokeWidth = 1.6f
            )
        }
    }

    private fun drawSakuraOverlay(scope: DrawScope, animTime: Float) {
        val petalColor = Color(0xAAF4A7B9)
        val petalCount = 25
        for (i in 0 until petalCount) {
            val baseX = ((i * 59) % scope.size.width.toInt()).toFloat()
            val swayX = sin(animTime * 2f + i) * 20f
            val yOffset = ((animTime * 60f) + i * 83) % scope.size.height
            scope.drawCircle(
                color = petalColor,
                radius = 2.5f,
                center = Offset(baseX + swayX, yOffset)
            )
        }
    }

    private fun drawMistOverlay(scope: DrawScope, animTime: Float) {
        val mistColor = Color(0x22EDE8E3)
        for (m in 0..2) {
            val mistY = scope.size.height * (0.35f + m * 0.2f)
            val driftX = (animTime * 15f * (m + 1)) % scope.size.width
            scope.drawOval(
                color = mistColor,
                topLeft = Offset(driftX - 150f, mistY),
                size = Size(300f, 40f)
            )
        }
    }
}
