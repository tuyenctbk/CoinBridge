package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.ui.calculator.ExpressionEvaluator.toCleanString
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.CurrencySelectionSheet
import com.example.ui.components.RateTrendChart
import com.example.ui.components.WidgetPreviewCard
import com.example.ui.theme.dpadFocusable
import com.example.ui.donation.DonationManager
import com.example.ui.viewmodel.CoinBridgeViewModel

@Composable
fun ConverterScreen(
    viewModel: CoinBridgeViewModel,
    onOpenDonationSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val targetCurrencies by viewModel.targetCurrencies.collectAsState()
    val expressionInput by viewModel.expressionInput.collectAsState()
    val evaluatedAmount by viewModel.evaluatedAmount.collectAsState()
    val convertedValues by viewModel.convertedValues.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()

    val showMilestoneBanner by DonationManager.showMilestoneBanner.collectAsState()
    val operationCount by DonationManager.operationCount.collectAsState()

    var showBaseSheet by remember { mutableStateOf(false) }
    var showTargetSheet by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(true) }

    if (showBaseSheet) {
        CurrencySelectionSheet(
            title = "Select Base Currency",
            onCurrencySelected = { viewModel.setBaseCurrency(it) },
            onDismiss = { showBaseSheet = false }
        )
    }

    if (showTargetSheet) {
        CurrencySelectionSheet(
            title = "Add Target Currency",
            onCurrencySelected = { viewModel.addTargetCurrency(it) },
            onDismiss = { showTargetSheet = false }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val isWideScreen = maxWidth >= 600.dp
        val isCompactHeight = maxHeight < 580.dp

        if (isWideScreen) {
            // Adaptive 2-Column Canonical Layout for Tablets & Android TV
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Base Currency + Target Cards List + Live Trends
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        // Rare Satisfaction Milestone Banner (Triggered after verified usage, cooldown guarded)
                        if (showMilestoneBanner) {
                            item {
                                MilestoneSatisfactionCard(
                                    operationCount = operationCount,
                                    onSayThanks = onOpenDonationSheet,
                                    onDismiss = { DonationManager.dismissMilestonePrompt(context) }
                                )
                            }
                        }

                        item {
                            BaseCurrencyCard(
                                baseCurrency = baseCurrency,
                                expressionInput = expressionInput,
                                evaluatedAmount = evaluatedAmount,
                                onOpenSheet = { showBaseSheet = true },
                                onToggleKeypad = { showKeypad = !showKeypad }
                            )
                        }

                        item {
                            TargetHeaderRow(onAddTarget = { showTargetSheet = true })
                        }

                        items(targetCurrencies, key = { it.code }) { target ->
                            val convertedVal = convertedValues[target.code] ?: 0.0
                            TargetCurrencyCard(
                                targetCurrency = target,
                                convertedValue = convertedVal,
                                context = context,
                                onSwap = { viewModel.swapBaseWithTarget(target) },
                                onRemove = { viewModel.removeTargetCurrency(target) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            LiveTrendsSection(
                                viewModel = viewModel,
                                baseCurrency = baseCurrency,
                                targetCurrencies = targetCurrencies
                            )
                        }

                        // On compact height (like Android TV 1080p landscape), place Widget preview here in scrollable list
                        if (isCompactHeight) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                val firstTarget = targetCurrencies.firstOrNull() ?: Currency.ALL_CURRENCIES.first()
                                val rate = convertedValues[firstTarget.code] ?: 1.0
                                WidgetPreviewCard(
                                    baseCurrency = baseCurrency,
                                    targetCurrency = firstTarget,
                                    rate = if (evaluatedAmount > 0) rate / evaluatedAmount else rate,
                                    isOffline = isOffline,
                                    onSwap = { viewModel.swapBaseWithTarget(firstTarget) },
                                    onRefresh = { viewModel.refreshRates() }
                                )
                            }
                        }
                    }
                }

                // Right Column: Dedicated Keypad (and Widget Preview if tall tablet)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = if (isCompactHeight) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    if (!isCompactHeight) {
                        val firstTarget = targetCurrencies.firstOrNull() ?: Currency.ALL_CURRENCIES.first()
                        val rate = convertedValues[firstTarget.code] ?: 1.0
                        WidgetPreviewCard(
                            baseCurrency = baseCurrency,
                            targetCurrency = firstTarget,
                            rate = if (evaluatedAmount > 0) rate / evaluatedAmount else rate,
                            isOffline = isOffline,
                            onSwap = { viewModel.swapBaseWithTarget(firstTarget) },
                            onRefresh = { viewModel.refreshRates() },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    CalculatorKeypad(
                        onKeyClick = { viewModel.onKeyInput(it) },
                        isCompact = isCompactHeight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        } else {
            // Mobile Vertical Fluid Layout
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Rare Satisfaction Milestone Banner
                    if (showMilestoneBanner) {
                        item {
                            MilestoneSatisfactionCard(
                                operationCount = operationCount,
                                onSayThanks = onOpenDonationSheet,
                                onDismiss = { DonationManager.dismissMilestonePrompt(context) }
                            )
                        }
                    }

                    item {
                        BaseCurrencyCard(
                            baseCurrency = baseCurrency,
                            expressionInput = expressionInput,
                            evaluatedAmount = evaluatedAmount,
                            onOpenSheet = { showBaseSheet = true },
                            onToggleKeypad = { showKeypad = !showKeypad }
                        )
                    }

                    item {
                        TargetHeaderRow(onAddTarget = { showTargetSheet = true })
                    }

                    items(targetCurrencies, key = { it.code }) { target ->
                        val convertedVal = convertedValues[target.code] ?: 0.0
                        TargetCurrencyCard(
                            targetCurrency = target,
                            convertedValue = convertedVal,
                            context = context,
                            onSwap = { viewModel.swapBaseWithTarget(target) },
                            onRemove = { viewModel.removeTargetCurrency(target) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        LiveTrendsSection(
                            viewModel = viewModel,
                            baseCurrency = baseCurrency,
                            targetCurrencies = targetCurrencies
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        val firstTarget = targetCurrencies.firstOrNull() ?: Currency.ALL_CURRENCIES.first()
                        val rate = convertedValues[firstTarget.code] ?: 1.0
                        WidgetPreviewCard(
                            baseCurrency = baseCurrency,
                            targetCurrency = firstTarget,
                            rate = if (evaluatedAmount > 0) rate / evaluatedAmount else rate,
                            isOffline = isOffline,
                            onSwap = { viewModel.swapBaseWithTarget(firstTarget) },
                            onRefresh = { viewModel.refreshRates() }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                AnimatedVisibility(visible = showKeypad) {
                    CalculatorKeypad(
                        onKeyClick = { viewModel.onKeyInput(it) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveTrendsSection(
    viewModel: CoinBridgeViewModel,
    baseCurrency: Currency,
    targetCurrencies: List<Currency>
) {
    val primaryTarget = targetCurrencies.firstOrNull() ?: Currency.ALL_CURRENCIES.find { it.code == "EUR" } ?: Currency.DEFAULT_BASE
    val trendPoints by viewModel.trendPoints.collectAsState()
    val trendFrom by viewModel.trendFromCurrency.collectAsState()
    val trendTo by viewModel.trendToCurrency.collectAsState()
    val trendDaysRange by viewModel.trendDaysRange.collectAsState()

    androidx.compose.runtime.LaunchedEffect(baseCurrency, primaryTarget) {
        viewModel.setTrendPair(baseCurrency, primaryTarget)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_trends_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📈", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Rate Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Timeframe Chips (7D, 30D, 90D)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(7 to "7D", 30 to "30D", 90 to "90D").forEach { (days, label) ->
                        val isSelected = trendDaysRange == days
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTrendDaysRange(days) }
                                .testTag("trend_chip_${days}d"),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            RateTrendChart(
                points = trendPoints,
                fromCode = trendFrom.code,
                toCode = trendTo.code,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_trends_chart")
            )
        }
    }
}

@Composable
private fun MilestoneSatisfactionCard(
    operationCount: Int,
    onSayThanks: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_satisfaction_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$operationCount Calculations Completed!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CoinBridge is 100% ad-free & offline-first. If it's helping you with your travels or trades, voluntary support keeps development independent!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSayThanks,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Say Thanks ☕", color = MaterialTheme.colorScheme.tertiaryContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TargetHeaderRow(onAddTarget: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Target Conversions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = onAddTarget,
            modifier = Modifier
                .testTag("add_target_btn")
                .dpadFocusable(shape = RoundedCornerShape(24.dp))
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Target")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Target")
        }
    }
}

@Composable
private fun BaseCurrencyCard(
    baseCurrency: Currency,
    expressionInput: String,
    evaluatedAmount: Double,
    onOpenSheet: () -> Unit,
    onToggleKeypad: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("base_currency_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .dpadFocusable(shape = RoundedCornerShape(16.dp))
                        .clickable { onOpenSheet() }
                        .testTag("base_currency_selector"),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(baseCurrency.flagOrIcon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = baseCurrency.code,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("▼", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(
                    onClick = onToggleKeypad,
                    modifier = Modifier
                        .testTag("toggle_keypad")
                        .dpadFocusable(shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keypad",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Expression Input
            AnimatedContent(
                targetState = if (expressionInput.isEmpty()) "0" else expressionInput,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ExpressionAnimation"
            ) { targetText ->
                Text(
                    text = targetText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expression_display")
                )
            }

            if (expressionInput.contains("+") || expressionInput.contains("-") ||
                expressionInput.contains("×") || expressionInput.contains("÷")
            ) {
                Text(
                    text = "= ${evaluatedAmount.toCleanString()} ${baseCurrency.code}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TargetCurrencyCard(
    targetCurrency: Currency,
    convertedValue: Double,
    context: Context,
    onSwap: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("target_currency_card_${targetCurrency.code}"),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(targetCurrency.flagOrIcon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = targetCurrency.code,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onSwap, modifier = Modifier.size(28.dp).testTag("swap_${targetCurrency.code}")) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Currency",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = targetCurrency.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Currency Value", convertedValue.toCleanString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied ${convertedValue.toCleanString()} to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "${targetCurrency.symbol} ${convertedValue.toCleanString()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("converted_val_${targetCurrency.code}")
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Currency Value", convertedValue.toCleanString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied ${convertedValue.toCleanString()} ${targetCurrency.code}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp).testTag("copy_${targetCurrency.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Value",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_${targetCurrency.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Target",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
