package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class CoinbaseData(
    @Json(name = "currency") val currency: String,
    @Json(name = "rates") val rates: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class CoinbaseRatesResponse(
    @Json(name = "data") val data: CoinbaseData
)

interface CoinbaseApi {
    @GET("exchange-rates")
    suspend fun getExchangeRates(
        @Query("currency") currency: String
    ): CoinbaseRatesResponse

    companion object {
        const val BASE_URL = "https://api.coinbase.com/v2/"
    }
}
