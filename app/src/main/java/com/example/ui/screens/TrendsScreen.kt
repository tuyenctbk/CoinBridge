package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencySelectionSheet
import com.example.ui.components.RateTrendChart
import com.example.ui.viewmodel.CoinBridgeViewModel

@Composable
fun TrendsScreen(
    viewModel: CoinBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val fromCurrency by viewModel.trendFromCurrency.collectAsState()
    val toCurrency by viewModel.trendToCurrency.collectAsState()
    val daysRange by viewModel.trendDaysRange.collectAsState()
    val trendPoints by viewModel.trendPoints.collectAsState()
    val isLoading by viewModel.trendIsLoading.collectAsState()

    var showFromSheet by remember { mutableStateOf(false) }
    var showToSheet by remember { mutableStateOf(false) }

    if (showFromSheet) {
        CurrencySelectionSheet(
            title = "Select From Currency",
            onCurrencySelected = { viewModel.setTrendPair(it, toCurrency) },
            onDismiss = { showFromSheet = false }
        )
    }

    if (showToSheet) {
        CurrencySelectionSheet(
            title = "Select Target Currency",
            onCurrencySelected = { viewModel.setTrendPair(fromCurrency, it) },
            onDismiss = { showToSheet = false }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isWideScreen = maxWidth >= 640.dp

        if (isWideScreen) {
            // Adaptive 2-Column Layout for Tablets & Foldables
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Currency Selectors & Horizon Range
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PairSelectionCard(
                        fromCurrency = fromCurrency,
                        toCurrency = toCurrency,
                        onOpenFromSheet = { showFromSheet = true },
                        onOpenToSheet = { showToSheet = true },
                        onSwap = { viewModel.setTrendPair(toCurrency, fromCurrency) }
                    )

                    TimeHorizonCard(
                        daysRange = daysRange,
                        onRangeSelected = { viewModel.setTrendDaysRange(it) }
                    )
                }

                // Right Column: Chart View
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    if (isLoading) {
                        LoadingChartCard()
                    } else {
                        RateTrendChart(
                            points = trendPoints,
                            fromCode = fromCurrency.code,
                            toCode = toCurrency.code
                        )
                    }
                }
            }
        } else {
            // Mobile Vertical Single Column Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PairSelectionCard(
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    onOpenFromSheet = { showFromSheet = true },
                    onOpenToSheet = { showToSheet = true },
                    onSwap = { viewModel.setTrendPair(toCurrency, fromCurrency) }
                )

                TimeHorizonCard(
                    daysRange = daysRange,
                    onRangeSelected = { viewModel.setTrendDaysRange(it) }
                )

                if (isLoading) {
                    LoadingChartCard()
                } else {
                    RateTrendChart(
                        points = trendPoints,
                        fromCode = fromCurrency.code,
                        toCode = toCurrency.code
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PairSelectionCard(
    fromCurrency: com.example.data.model.Currency,
    toCurrency: com.example.data.model.Currency,
    onOpenFromSheet: () -> Unit,
    onOpenToSheet: () -> Unit,
    onSwap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenFromSheet() }
                    .testTag("trend_from_btn"),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fromCurrency.flagOrIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(fromCurrency.code, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onSwap,
                modifier = Modifier.testTag("trend_swap_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Pair",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenToSheet() }
                    .testTag("trend_to_btn"),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(toCurrency.flagOrIcon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(toCurrency.code, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimeHorizonCard(
    daysRange: Int,
    onRangeSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Time Horizon",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ranges = listOf(7 to "7D", 30 to "30D", 90 to "90D", 365 to "1Y")
                ranges.forEach { (days, label) ->
                    FilterChip(
                        selected = daysRange == days,
                        onClick = { onRangeSelected(days) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("range_chip_$days")
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingChartCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Text("Fetching exchange rate trends...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
