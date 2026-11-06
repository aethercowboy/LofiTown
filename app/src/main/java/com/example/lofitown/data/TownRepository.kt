package com.example.lofitown.data

import com.example.lofitown.model.BuildingType
import com.example.lofitown.model.TerrainType
import com.example.lofitown.model.TownTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TownRepository(private val dao: TownDao) {
    val gridSize = 6

    val tilesFlow: Flow<List<TownTile>> = dao.getAllTiles().map { entities ->
        if (entities.isEmpty()) {
            emptyList()
        } else {
            entities.map { it.toDomain() }
        }
    }

    suspend fun initializeDefaultTownIfNeeded(): List<TownTile> = withContext(Dispatchers.IO) {
        val existing = dao.getAllTilesSync()
        if (existing.isNotEmpty()) {
            return@withContext existing.map { it.toDomain() }
        }

        val defaultTiles = mutableListOf<TownTile>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                var terrain = TerrainType.MEADOW
                var building: BuildingType? = null

                // Peaceful river flowing along y = 3
                if (y == 3) {
                    terrain = if (x == 2) TerrainType.BRIDGE else TerrainType.RIVER
                } else if (x == 2) {
                    // Cobblestone path connecting across the bridge
                    terrain = TerrainType.COBBLESTONE
                } else if (y == 2 && x in 1..4) {
                    terrain = TerrainType.COBBLESTONE
                }

                // Initial starter buildings and decor
                if (x == 1 && y == 1) {
                    building = BuildingType.COFFEE_SHOP
                } else if (x == 3 && y == 1) {
                    terrain = TerrainType.SAKURA_TREE
                } else if (x == 1 && y == 4) {
                    terrain = TerrainType.LAMP_POST
                } else if (x == 3 && y == 4) {
                    terrain = TerrainType.PARK_BENCH
                }

                defaultTiles.add(TownTile(x, y, terrain, building, level = 1))
            }
        }

        val entities = defaultTiles.map { it.toEntity() }
        dao.insertAllTiles(entities)

        // Default meta
        dao.setMeta(TownMetaEntity("vibes", doubleValue = 50.0))
        dao.setMeta(TownMetaEntity("zen_stars", longValue = 10L))
        dao.setMeta(TownMetaEntity("time_minute", longValue = 18 * 60L + 30L)) // 18:30 Dusk
        dao.setMeta(TownMetaEntity("last_saved_time", longValue = System.currentTimeMillis()))

        defaultTiles
    }

    suspend fun saveTile(tile: TownTile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateTile(tile.toEntity())
    }

    suspend fun getVibes(): Double = withContext(Dispatchers.IO) {
        dao.getMeta("vibes")?.doubleValue ?: 50.0
    }

    suspend fun saveVibes(vibes: Double) = withContext(Dispatchers.IO) {
        dao.setMeta(TownMetaEntity("vibes", doubleValue = vibes))
    }

    suspend fun getZenStars(): Long = withContext(Dispatchers.IO) {
        dao.getMeta("zen_stars")?.longValue ?: 10L
    }

    suspend fun saveZenStars(stars: Long) = withContext(Dispatchers.IO) {
        dao.setMeta(TownMetaEntity("zen_stars", longValue = stars))
    }

    suspend fun getTimeMinute(): Long = withContext(Dispatchers.IO) {
        dao.getMeta("time_minute")?.longValue ?: (18 * 60L + 30L)
    }

    suspend fun saveTimeMinute(minute: Long) = withContext(Dispatchers.IO) {
        dao.setMeta(TownMetaEntity("time_minute", longValue = minute))
    }

    suspend fun getLastSavedTime(): Long = withContext(Dispatchers.IO) {
        dao.getMeta("last_saved_time")?.longValue ?: System.currentTimeMillis()
    }

    suspend fun updateLastSavedTime() = withContext(Dispatchers.IO) {
        dao.setMeta(TownMetaEntity("last_saved_time", longValue = System.currentTimeMillis()))
    }

    private fun TownTile.toEntity(): TileEntity {
        return TileEntity(
            id = "${x}_${y}",
            x = x,
            y = y,
            terrainName = terrain.name,
            buildingId = building?.id,
            level = level
        )
    }

    private fun TileEntity.toDomain(): TownTile {
        val t = try {
            TerrainType.valueOf(terrainName)
        } catch (e: Exception) {
            TerrainType.MEADOW
        }
        val b = buildingId?.let { id ->
            BuildingType.entries.firstOrNull { it.id == id }
        }
        return TownTile(x, y, t, b, level)
    }
}
