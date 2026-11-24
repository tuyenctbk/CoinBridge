package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class FrankfurterLatestResponse(
    @param:Json(name = "amount") val amount: Double = 1.0,
    @param:Json(name = "base") val base: String,
    @param:Json(name = "date") val date: String,
    @param:Json(name = "rates") val rates: Map<String, Double>
)

@JsonClass(generateAdapter = true)
data class FrankfurterHistoricalResponse(
    @param:Json(name = "amount") val amount: Double = 1.0,
    @param:Json(name = "base") val base: String,
    @param:Json(name = "start_date") val startDate: String,
    @param:Json(name = "end_date") val endDate: String,
    @param:Json(name = "rates") val rates: Map<String, Map<String, Double>>
)

interface FrankfurterApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") from: String,
        @Query("to") to: String? = null
    ): FrankfurterLatestResponse

    @GET("{startDate}..{endDate}")
    suspend fun getTimeSeries(
        @Path("startDate") startDate: String,
        @Path("endDate") endDate: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): FrankfurterHistoricalResponse

    companion object {
        const val BASE_URL = "https://api.frankfurter.app/"
    }
}
