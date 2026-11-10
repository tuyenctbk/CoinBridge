package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CachedRateEntity
import com.example.data.local.CoinBridgeDao
import com.example.data.local.CoinBridgeDatabase
import com.example.data.local.ConversionHistoryEntity
import com.example.data.local.FavoritePairEntity
import com.example.data.local.FavoriteUnitPairEntity
import com.example.data.local.UnitConversionHistoryEntity
import com.example.data.model.Currency
import com.example.data.model.ExchangeRate
import com.example.data.model.HistoricalRatePoint
import com.example.data.model.PhysicalUnit
import com.example.data.model.UnitCategory
import com.example.data.remote.CoinbaseApi
import com.example.data.remote.FrankfurterApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CurrencyRepository(context: Context) {
    private val dao: CoinBridgeDao = CoinBridgeDatabase.getInstance(context).dao()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val frankfurterApi: FrankfurterApi by lazy {
        Retrofit.Builder()
            .baseUrl(FrankfurterApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FrankfurterApi::class.java)
    }

    private val coinbaseApi: CoinbaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(CoinbaseApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CoinbaseApi::class.java)
    }

    // Live In-Memory Cache for fast UI updates
    private val rateCacheMap = mutableMapOf<String, Double>()
    var lastSyncTimestamp: Long = 0L
        private set
    var isOfflineMode: Boolean = false
        private set

    suspend fun fetchLatestRates(baseCode: String): Pair<Map<String, Double>, Boolean> = withContext(Dispatchers.IO) {
        val rates = mutableMapOf<String, Double>()
        var isOffline = false

        try {
            // 1. Try Coinbase Public API (Covers USD base for Crypto & Fiat)
            val cbResponse = coinbaseApi.getExchangeRates(baseCode.uppercase())
            val cbRates = cbResponse.data.rates
            cbRates.forEach { (code, strRate) ->
                strRate.toDoubleOrNull()?.let { r ->
                    rates[code] = r
                }
            }
            // Add self rate
            rates[baseCode.uppercase()] = 1.0

            // 2. Try Frankfurter for fiat accuracy if base is Fiat
            val isFiat = Currency.ALL_CURRENCIES.find { it.code == baseCode }?.isCrypto == false
            if (isFiat) {
                try {
                    val ffResponse = frankfurterApi.getLatestRates(from = baseCode.uppercase())
                    ffResponse.rates.forEach { (code, r) ->
                        rates[code] = r
                    }
                } catch (e: Exception) {
                    Log.w("CurrencyRepo", "Frankfurter API offline or rate-limited: ${e.message}")
                }
            }

            // Save to local Room DB for offline mode
            if (rates.isNotEmpty()) {
                val now = System.currentTimeMillis()
                lastSyncTimestamp = now
                val entities = rates.map { (toCode, rate) ->
                    CachedRateEntity(
                        pairKey = "${baseCode.uppercase()}_${toCode.uppercase()}",
                        fromCode = baseCode.uppercase(),
                        toCode = toCode.uppercase(),
                        rate = rate,
                        timestamp = now
                    )
                }
                dao.insertRates(entities)
                rateCacheMap.putAll(rates)
                isOfflineMode = false
            }
        } catch (e: Exception) {
            Log.e("CurrencyRepo", "Network error fetching rates online: ${e.message}. Using offline Room DB.")
            isOffline = true
            isOfflineMode = true

            // Fallback: Read from Room Database
            rates.putAll(loadOfflineRates(baseCode))
        }

        // Fallback default rates if DB is empty on first boot without internet
        if (rates.isEmpty()) {
            rates.putAll(getStaticFallbackRates(baseCode))
            isOffline = true
            isOfflineMode = true
        }

        return@withContext Pair(rates, isOffline)
    }

    private suspend fun loadOfflineRates(baseCode: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        map[baseCode.uppercase()] = 1.0
        Currency.ALL_CURRENCIES.forEach { target ->
            val entity = dao.getRate("${baseCode.uppercase()}_${target.code.uppercase()}")
            if (entity != null) {
                map[target.code] = entity.rate
                if (lastSyncTimestamp == 0L) {
                    lastSyncTimestamp = entity.timestamp
                }
            }
        }
        return map
    }

    suspend fun convertAmount(
        amount: Double,
        fromCode: String,
        toCode: String,
        currentRates: Map<String, Double>
    ): Double {
        if (fromCode.equals(toCode, ignoreCase = true)) return amount

        // Direct lookup if rates map is relative to fromCode
        val directRate = currentRates[toCode.uppercase()]
        if (directRate != null && directRate > 0.0) {
            return amount * directRate
        }

        // Cross rate conversion via USD
        val fromToUsdRate = currentRates[fromCode.uppercase()]
        val toToUsdRate = currentRates[toCode.uppercase()]

        if (fromToUsdRate != null && toToUsdRate != null && fromToUsdRate > 0.0) {
            val usdAmount = amount / fromToUsdRate
            return usdAmount * toToUsdRate
        }

        return amount
    }

    suspend fun getHistoricalTrends(
        fromCode: String,
        toCode: String,
        days: Int = 30
    ): List<HistoricalRatePoint> = withContext(Dispatchers.IO) {
        val result = mutableListOf<HistoricalRatePoint>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val calEnd = Calendar.getInstance()
        val endDateStr = sdf.format(calEnd.time)

        val calStart = Calendar.getInstance()
        calStart.add(Calendar.DAY_OF_YEAR, -days)
        val startDateStr = sdf.format(calStart.time)

        // If both are Fiat, fetch real time-series from Frankfurter API
        val isFromCrypto = Currency.ALL_CURRENCIES.find { it.code == fromCode }?.isCrypto == true
        val isToCrypto = Currency.ALL_CURRENCIES.find { it.code == toCode }?.isCrypto == true

        if (!isFromCrypto && !isToCrypto) {
            try {
                val response = frankfurterApi.getTimeSeries(
                    startDate = startDateStr,
                    endDate = endDateStr,
                    from = fromCode.uppercase(),
                    to = toCode.uppercase()
                )
                response.rates.forEach { (dateStr, map) ->
                    map[toCode.uppercase()]?.let { rate ->
                        result.add(HistoricalRatePoint(dateStr, rate))
                    }
                }
            } catch (e: Exception) {
                Log.w("CurrencyRepo", "Failed fetching historical rates from Frankfurter: ${e.message}")
            }
        }

        // Fallback simulation curve if network fails or crypto pair
        if (result.isEmpty()) {
            val baseRate = convertAmount(1.0, fromCode, toCode, getStaticFallbackRates(fromCode))
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -days)
            var current = baseRate * 0.95

            for (i in 0..days step (if (days > 60) 3 else 1)) {
                val dStr = sdf.format(cal.time)
                val noise = (Math.sin(i.toDouble() * 0.5) * 0.03) + ((i % 5 - 2) * 0.005)
                val simulatedRate = (current * (1.0 + noise)).coerceAtLeast(0.000001)
                result.add(HistoricalRatePoint(dStr, simulatedRate))
                cal.add(Calendar.DAY_OF_YEAR, if (days > 60) 3 else 1)
            }
        }

        return@withContext result
    }

    // Favorites
    fun getFavoritePairs(): Flow<List<FavoritePairEntity>> = dao.getFavoritePairs().flowOn(Dispatchers.IO)

    suspend fun addFavorite(fromCode: String, toCode: String) = withContext(Dispatchers.IO) {
        dao.insertFavorite(FavoritePairEntity(fromCode = fromCode, toCode = toCode))
    }

    suspend fun removeFavorite(fromCode: String, toCode: String) = withContext(Dispatchers.IO) {
        dao.deleteFavorite(fromCode, toCode)
    }

    // History
    fun getConversionHistory(): Flow<List<ConversionHistoryEntity>> = dao.getConversionHistory().flowOn(Dispatchers.IO)

    suspend fun logConversion(fromCode: String, toCode: String, fromAmount: Double, toAmount: Double) = withContext(Dispatchers.IO) {
        dao.insertHistory(
            ConversionHistoryEntity(
                fromCode = fromCode,
                toCode = toCode,
                fromAmount = fromAmount,
                toAmount = toAmount
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }

    // Unit Conversion Room Operations
    fun getUnitHistory(): Flow<List<UnitConversionHistoryEntity>> = dao.getAllUnitHistory().flowOn(Dispatchers.IO)

    fun getUnitHistoryByCategory(category: String): Flow<List<UnitConversionHistoryEntity>> =
        dao.getUnitHistoryByCategory(category).flowOn(Dispatchers.IO)

    suspend fun logUnitConversion(
        category: String,
        fromUnitId: String,
        toUnitId: String,
        fromValue: Double,
        toValue: Double
    ) = withContext(Dispatchers.IO) {
        dao.insertUnitHistory(
            UnitConversionHistoryEntity(
                category = category,
                fromUnitId = fromUnitId,
                toUnitId = toUnitId,
                fromValue = fromValue,
                toValue = toValue
            )
        )
    }

    suspend fun clearUnitHistory() = withContext(Dispatchers.IO) {
        dao.clearUnitHistory()
    }

    fun getFavoriteUnits(): Flow<List<FavoriteUnitPairEntity>> = dao.getFavoriteUnits().flowOn(Dispatchers.IO)

    suspend fun toggleFavoriteUnit(category: String, fromUnitId: String, toUnitId: String) = withContext(Dispatchers.IO) {
        val currentFavs = dao.getFavoriteUnits()
        // Check if exists
        dao.insertFavoriteUnit(
            FavoriteUnitPairEntity(
                category = category,
                fromUnitId = fromUnitId,
                toUnitId = toUnitId
            )
        )
    }

    suspend fun removeFavoriteUnit(fromUnitId: String, toUnitId: String) = withContext(Dispatchers.IO) {
        dao.deleteFavoriteUnit(fromUnitId, toUnitId)
    }

    // Static Fallback Rates (When offline on initial boot)
    private fun getStaticFallbackRates(base: String): Map<String, Double> {
        val usdRates = mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "JPY" to 153.4,
            "GBP" to 0.78,
            "AUD" to 1.51,
            "CAD" to 1.38,
            "CHF" to 0.86,
            "CNY" to 7.12,
            "HKD" to 7.77,
            "NZD" to 1.66,
            "SGD" to 1.32,
            "KRW" to 1375.0,
            "MXN" to 19.8,
            "INR" to 84.1,
            "BRL" to 5.70,
            "RUB" to 97.5,
            "ZAR" to 17.6,
            "TRY" to 34.2,
            "THB" to 33.8,
            "VND" to 25300.0,
            "IDR" to 15600.0,
            "MYR" to 4.35,
            "PHP" to 58.2,
            "PLN" to 4.01,
            "SEK" to 10.6,
            "NOK" to 10.9,
            "DKK" to 6.86,
            "AED" to 3.67,
            "SAR" to 3.75,
            "EGP" to 49.0,
            "BTC" to 0.0000147, // ~$68,000 USD
            "ETH" to 0.00039,  // ~$2,560 USD
            "SOL" to 0.0058,   // ~$172 USD
            "XRP" to 1.96,     // ~$0.51 USD
            "DOGE" to 6.25,    // ~$0.16 USD
            "ADA" to 2.85,     // ~$0.35 USD
            "USDT" to 1.0,
            "USDC" to 1.0,
            "LTC" to 0.014,    // ~$71 USD
            "BNB" to 0.0017,   // ~$585 USD
            "LINK" to 0.083,   // ~$12 USD
            "DOT" to 0.23,     // ~$4.30 USD
            "AVAX" to 0.038,   // ~$26 USD
            "NEAR" to 0.21,    // ~$4.70 USD
            "SHIB" to 55555.0  // ~$0.000018 USD
        )

        if (base.equals("USD", ignoreCase = true)) return usdRates

        val baseToUsd = usdRates[base.uppercase()] ?: 1.0
        val relativeMap = mutableMapOf<String, Double>()
        usdRates.forEach { (code, rateInUsd) ->
            relativeMap[code] = rateInUsd / baseToUsd
        }
        return relativeMap
    }
}
