package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillSplitResult
import com.example.data.model.Currency
import com.example.ui.calculator.ExpressionEvaluator.toCleanString
import com.example.ui.components.CurrencySelectionSheet
import com.example.ui.viewmodel.CoinBridgeViewModel

@Composable
fun BillSplitterScreen(
    viewModel: CoinBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val billAmountInput by viewModel.billAmountInput.collectAsState()
    val tipPercentage by viewModel.billTipPercentage.collectAsState()
    val splitCount by viewModel.billSplitCount.collectAsState()
    val foreignCurrency by viewModel.billForeignCurrency.collectAsState()
    val homeCurrency by viewModel.billHomeCurrency.collectAsState()
    val splitResult by viewModel.billSplitResult.collectAsState()

    var showForeignSheet by remember { mutableStateOf(false) }
    var showHomeSheet by remember { mutableStateOf(false) }

    if (showForeignSheet) {
        CurrencySelectionSheet(
            title = "Select Bill Foreign Currency",
            onCurrencySelected = { viewModel.setBillForeignCurrency(it) },
            onDismiss = { showForeignSheet = false }
        )
    }

    if (showHomeSheet) {
        CurrencySelectionSheet(
            title = "Select Home Currency",
            onCurrencySelected = { viewModel.setBillHomeCurrency(it) },
            onDismiss = { showHomeSheet = false }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val isWideScreen = maxWidth >= 640.dp

        if (isWideScreen) {
            // Adaptive Two-Column Layout for Tablets & Foldables
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Inputs & Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BillDetailsCard(
                        billAmountInput = billAmountInput,
                        foreignCurrency = foreignCurrency,
                        homeCurrency = homeCurrency,
                        onAmountChanged = { viewModel.setBillAmount(it) },
                        onOpenForeignSheet = { showForeignSheet = true },
                        onOpenHomeSheet = { showHomeSheet = true }
                    )

                    TipSelectorCard(
                        tipPercentage = tipPercentage,
                        onTipSelected = { viewModel.setBillTipPercentage(it) }
                    )

                    SplitCounterCard(
                        splitCount = splitCount,
                        onCountChanged = { viewModel.setBillSplitCount(it) }
                    )
                }

                // Right Column: Title Card & Summary Result
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroTitleCard()

                    splitResult?.let { res ->
                        BillResultCard(res = res)
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
                HeroTitleCard()

                BillDetailsCard(
                    billAmountInput = billAmountInput,
                    foreignCurrency = foreignCurrency,
                    homeCurrency = homeCurrency,
                    onAmountChanged = { viewModel.setBillAmount(it) },
                    onOpenForeignSheet = { showForeignSheet = true },
                    onOpenHomeSheet = { showHomeSheet = true }
                )

                TipSelectorCard(
                    tipPercentage = tipPercentage,
                    onTipSelected = { viewModel.setBillTipPercentage(it) }
                )

                SplitCounterCard(
                    splitCount = splitCount,
                    onCountChanged = { viewModel.setBillSplitCount(it) }
                )

                splitResult?.let { res ->
                    BillResultCard(res = res)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HeroTitleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = "Bill Splitter",
                modifier = Modifier.padding(end = 14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column {
                Text(
                    text = "Travel Bill Splitter & Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Calculate foreign restaurant & taxi bills, add tips, and split directly in your home currency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun BillDetailsCard(
    billAmountInput: String,
    foreignCurrency: Currency,
    homeCurrency: Currency,
    onAmountChanged: (String) -> Unit,
    onOpenForeignSheet: () -> Unit,
    onOpenHomeSheet: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Bill Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Bill Input + Foreign Currency Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = billAmountInput,
                    onValueChange = onAmountChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bill_amount_input"),
                    label = { Text("Original Bill Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenForeignSheet() }
                        .testTag("foreign_currency_btn"),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(foreignCurrency.flagOrIcon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(foreignCurrency.code, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Home Currency Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Convert to Home Currency:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenHomeSheet() }
                        .testTag("home_currency_btn"),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(homeCurrency.flagOrIcon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(homeCurrency.code, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TipSelectorCard(
    tipPercentage: Float,
    onTipSelected: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tip Percentage",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val tips = listOf(0f, 10f, 15f, 18f, 20f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tips.forEach { pct ->
                    FilterChip(
                        selected = tipPercentage == pct,
                        onClick = { onTipSelected(pct) },
                        label = { Text("${pct.toInt()}%") },
                        modifier = Modifier.testTag("tip_chip_${pct.toInt()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitCounterCard(
    splitCount: Int,
    onCountChanged: (Int) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = "People", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Split Among",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onCountChanged(splitCount - 1) },
                    modifier = Modifier.testTag("split_minus")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = "$splitCount ${if (splitCount == 1) "person" else "people"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp).testTag("split_count_text")
                )

                IconButton(
                    onClick = { onCountChanged(splitCount + 1) },
                    modifier = Modifier.testTag("split_plus")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

@Composable
private fun BillResultCard(res: BillSplitResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_result_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Split Calculation Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Per-Person Highlight
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EACH PERSON PAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${res.toCurrency.symbol} ${res.perPersonConverted.toCleanString()} ${res.toCurrency.code}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("per_person_converted_text")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "(${res.fromCurrency.symbol} ${res.perPersonOriginal.toCleanString()} ${res.fromCurrency.code})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Bill (incl. tip):")
                Text(
                    "${res.fromCurrency.symbol} ${res.totalBillAmount.toCleanString()} ${res.fromCurrency.code}",
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tip Amount (${res.tipPercentage.toInt()}%):")
                Text(
                    "${res.fromCurrency.symbol} ${res.tipAmount.toCleanString()} ${res.fromCurrency.code}",
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Conversion Rate:")
                Text(
                    "1 ${res.fromCurrency.code} = ${res.conversionRate.toCleanString()} ${res.toCurrency.code}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
