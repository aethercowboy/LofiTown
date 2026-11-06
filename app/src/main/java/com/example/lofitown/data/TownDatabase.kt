package com.example.lofitown.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TileEntity::class, TownMetaEntity::class], version = 1, exportSchema = false)
abstract class TownDatabase : RoomDatabase() {
    abstract fun townDao(): TownDao

    companion object {
        @Volatile
        private var INSTANCE: TownDatabase? = null

        fun getInstance(context: Context): TownDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TownDatabase::class.java,
                    "lofi_town.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
