package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_rates")
data class CachedRateEntity(
    @PrimaryKey val pairKey: String, // e.g. "USD_EUR" or "BTC_USD"
    val fromCode: String,
    val toCode: String,
    val rate: Double,
    val timestamp: Long
)

@Entity(tableName = "favorite_pairs")
data class FavoritePairEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromCode: String,
    val toCode: String
)

@Entity(tableName = "conversion_history")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val fromCode: String,
    val toCode: String,
    val fromAmount: Double,
    val toAmount: Double,
    val note: String = ""
)

@Entity(tableName = "unit_conversion_history")
data class UnitConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // "WEIGHT", "LENGTH"
    val fromUnitId: String,
    val toUnitId: String,
    val fromValue: Double,
    val toValue: Double
)

@Entity(tableName = "favorite_unit_pairs")
data class FavoriteUnitPairEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val fromUnitId: String,
    val toUnitId: String
)

