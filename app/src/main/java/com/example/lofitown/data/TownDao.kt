package com.example.lofitown.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TownDao {
    @Query("SELECT * FROM tiles")
    fun getAllTiles(): Flow<List<TileEntity>>

    @Query("SELECT * FROM tiles")
    suspend fun getAllTilesSync(): List<TileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTile(tile: TileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTiles(tiles: List<TileEntity>)

    @Query("SELECT * FROM town_meta WHERE `key` = :key LIMIT 1")
    suspend fun getMeta(key: String): TownMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMeta(meta: TownMetaEntity)

    @Query("DELETE FROM tiles")
    suspend fun clearTiles()
}
