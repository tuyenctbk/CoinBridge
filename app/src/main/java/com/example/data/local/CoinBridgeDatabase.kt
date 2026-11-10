package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedRateEntity::class,
        FavoritePairEntity::class,
        ConversionHistoryEntity::class,
        UnitConversionHistoryEntity::class,
        FavoriteUnitPairEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CoinBridgeDatabase : RoomDatabase() {
    abstract fun dao(): CoinBridgeDao

    companion object {
        @Volatile
        private var INSTANCE: CoinBridgeDatabase? = null

        fun getInstance(context: Context): CoinBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoinBridgeDatabase::class.java,
                    "coinbridge_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
