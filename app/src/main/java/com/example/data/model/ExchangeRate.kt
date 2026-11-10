package com.example.data.model

data class ExchangeRate(
    val fromCode: String,
    val toCode: String,
    val rate: Double,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isOfflineCached: Boolean = false
)

data class HistoricalRatePoint(
    val date: String,
    val rate: Double
)

data class BillSplitResult(
    val rawAmount: Double,
    val tipPercentage: Float,
    val tipAmount: Double,
    val totalBillAmount: Double,
    val splitCount: Int,
    val perPersonOriginal: Double,
    val perPersonConverted: Double,
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val conversionRate: Double
)
