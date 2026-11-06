package com.example.lofitown.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tiles")
data class TileEntity(
    @PrimaryKey val id: String, // format: "x_y"
    val x: Int,
    val y: Int,
    val terrainName: String,
    val buildingId: String?,
    val level: Int
)

@Entity(tableName = "town_meta")
data class TownMetaEntity(
    @PrimaryKey val key: String,
    val longValue: Long = 0L,
    val doubleValue: Double = 0.0,
    val stringValue: String = ""
)
