package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinBridgeDao {
    // Rates Cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<CachedRateEntity>)

    @Query("SELECT * FROM cached_rates WHERE pairKey = :pairKey LIMIT 1")
    suspend fun getRate(pairKey: String): CachedRateEntity?

    @Query("SELECT * FROM cached_rates")
    fun getAllCachedRates(): Flow<List<CachedRateEntity>>

    @Query("DELETE FROM cached_rates")
    suspend fun clearRates()

    // Favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePairEntity)

    @Query("DELETE FROM favorite_pairs WHERE fromCode = :fromCode AND toCode = :toCode")
    suspend fun deleteFavorite(fromCode: String, toCode: String)

    @Query("SELECT * FROM favorite_pairs")
    fun getFavoritePairs(): Flow<List<FavoritePairEntity>>

    // Conversion History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConversionHistoryEntity)

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT 50")
    fun getConversionHistory(): Flow<List<ConversionHistoryEntity>>

    @Query("DELETE FROM conversion_history")
    suspend fun clearHistory()

    // Unit Conversion History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitHistory(history: UnitConversionHistoryEntity)

    @Query("SELECT * FROM unit_conversion_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllUnitHistory(): Flow<List<UnitConversionHistoryEntity>>

    @Query("SELECT * FROM unit_conversion_history WHERE category = :category ORDER BY timestamp DESC LIMIT 30")
    fun getUnitHistoryByCategory(category: String): Flow<List<UnitConversionHistoryEntity>>

    @Query("DELETE FROM unit_conversion_history")
    suspend fun clearUnitHistory()

    // Favorite Unit Pairs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteUnit(favorite: FavoriteUnitPairEntity)

    @Query("DELETE FROM favorite_unit_pairs WHERE fromUnitId = :fromUnitId AND toUnitId = :toUnitId")
    suspend fun deleteFavoriteUnit(fromUnitId: String, toUnitId: String)

    @Query("SELECT * FROM favorite_unit_pairs")
    fun getFavoriteUnits(): Flow<List<FavoriteUnitPairEntity>>
}
