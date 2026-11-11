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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UnitConversionHistoryEntity
import com.example.data.model.PhysicalUnit
import com.example.data.model.UnitCategory
import com.example.ui.theme.dpadFocusable
import com.example.ui.calculator.ExpressionEvaluator.toCleanString
import com.example.ui.components.CalculatorKeypad
import com.example.ui.viewmodel.CoinBridgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UnitConverterScreen(
    viewModel: CoinBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unitCategory by viewModel.unitCategory.collectAsState()
    val unitFrom by viewModel.unitFrom.collectAsState()
    val unitTo by viewModel.unitTo.collectAsState()
    val unitExpressionInput by viewModel.unitExpressionInput.collectAsState()
    val unitEvaluatedAmount by viewModel.unitEvaluatedAmount.collectAsState()
    val unitConvertedValue by viewModel.unitConvertedValue.collectAsState()
    val unitEquivalents by viewModel.unitEquivalents.collectAsState()
    val unitHistory by viewModel.unitConversionHistory.collectAsState()
    val favoriteUnits by viewModel.favoriteUnits.collectAsState()

    var showFromSheet by remember { mutableStateOf(false) }
    var showToSheet by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(true) }

    val isFavoritePair = favoriteUnits.any { it.fromUnitId == unitFrom.id && it.toUnitId == unitTo.id }

    if (showFromSheet) {
        UnitSelectionSheet(
            title = "Select Source Unit (${unitCategory.displayName})",
            units = PhysicalUnit.getUnitsForCategory(unitCategory),
            onUnitSelected = {
                viewModel.setUnitFrom(it)
                showFromSheet = false
            },
            onDismiss = { showFromSheet = false }
        )
    }

    if (showToSheet) {
        UnitSelectionSheet(
            title = "Select Target Unit (${unitCategory.displayName})",
            units = PhysicalUnit.getUnitsForCategory(unitCategory),
            onUnitSelected = {
                viewModel.setUnitTo(it)
                showToSheet = false
            },
            onDismiss = { showToSheet = false }
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
            // Tablet & Android TV 2-Column Canonical Layout
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Category Tabs, Conversion Cards, All Equivalents, History
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

                        // Category Switcher
                        item {
                            UnitCategorySelector(
                                selectedCategory = unitCategory,
                                onSelectCategory = { viewModel.setUnitCategory(it) }
                            )
                        }

                        // From Unit Card
                        item {
                            SourceUnitCard(
                                unit = unitFrom,
                                expressionInput = unitExpressionInput,
                                evaluatedAmount = unitEvaluatedAmount,
                                onOpenPicker = { showFromSheet = true },
                                onToggleKeypad = { showKeypad = !showKeypad }
                            )
                        }

                        // Swap Button Row
                        item {
                            SwapUnitsRow(onSwap = { viewModel.swapUnits() })
                        }

                        // To Unit Card
                        item {
                            TargetUnitCard(
                                fromUnit = unitFrom,
                                toUnit = unitTo,
                                convertedValue = unitConvertedValue,
                                isFavorite = isFavoritePair,
                                onOpenPicker = { showToSheet = true },
                                onToggleFavorite = { viewModel.toggleFavoriteUnit(unitFrom.id, unitTo.id) },
                                onCopy = {
                                    copyToClipboard(context, "${PhysicalUnit.formatUnitValue(unitConvertedValue)} ${unitTo.symbol}")
                                }
                            )
                        }

                        // Live Equivalents Matrix
                        item {
                            AllEquivalentsMatrixCard(
                                category = unitCategory,
                                currentToUnitId = unitTo.id,
                                equivalents = unitEquivalents,
                                onSelectTargetUnit = { unit -> viewModel.setUnitTo(unit) }
                            )
                        }

                        if (isCompactHeight) {
                            item {
                                UnitRecentHistoryCard(
                                    historyList = unitHistory,
                                    onSelectHistory = { history ->
                                        val f = PhysicalUnit.find(history.fromUnitId)
                                        val t = PhysicalUnit.find(history.toUnitId)
                                        viewModel.setUnitCategory(f.category)
                                        viewModel.setUnitFrom(f)
                                        viewModel.setUnitTo(t)
                                    },
                                    onClearHistory = { viewModel.clearUnitHistory() },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }

                // Right Column: Dedicated Keypad (and History if tall tablet)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = if (isCompactHeight) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    if (!isCompactHeight) {
                        UnitRecentHistoryCard(
                            historyList = unitHistory,
                            onSelectHistory = { history ->
                                val f = PhysicalUnit.find(history.fromUnitId)
                                val t = PhysicalUnit.find(history.toUnitId)
                                viewModel.setUnitCategory(f.category)
                                viewModel.setUnitFrom(f)
                                viewModel.setUnitTo(t)
                            },
                            onClearHistory = { viewModel.clearUnitHistory() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp)
                        )
                    }

                    CalculatorKeypad(
                        onKeyClick = { viewModel.onUnitKeyInput(it) },
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

                    // Category Switcher
                    item {
                        UnitCategorySelector(
                            selectedCategory = unitCategory,
                            onSelectCategory = { viewModel.setUnitCategory(it) }
                        )
                    }

                    // From Unit Card
                    item {
                        SourceUnitCard(
                            unit = unitFrom,
                            expressionInput = unitExpressionInput,
                            evaluatedAmount = unitEvaluatedAmount,
                            onOpenPicker = { showFromSheet = true },
                            onToggleKeypad = { showKeypad = !showKeypad }
                        )
                    }

                    // Swap Button Row
                    item {
                        SwapUnitsRow(onSwap = { viewModel.swapUnits() })
                    }

                    // To Unit Card
                    item {
                        TargetUnitCard(
                            fromUnit = unitFrom,
                            toUnit = unitTo,
                            convertedValue = unitConvertedValue,
                            isFavorite = isFavoritePair,
                            onOpenPicker = { showToSheet = true },
                            onToggleFavorite = { viewModel.toggleFavoriteUnit(unitFrom.id, unitTo.id) },
                            onCopy = {
                                copyToClipboard(context, "${PhysicalUnit.formatUnitValue(unitConvertedValue)} ${unitTo.symbol}")
                            }
                        )
                    }

                    // Live Equivalents Matrix
                    item {
                        AllEquivalentsMatrixCard(
                            category = unitCategory,
                            currentToUnitId = unitTo.id,
                            equivalents = unitEquivalents,
                            onSelectTargetUnit = { unit -> viewModel.setUnitTo(unit) }
                        )
                    }

                    // Recent Unit History Card
                    if (unitHistory.isNotEmpty()) {
                        item {
                            UnitRecentHistoryCard(
                                historyList = unitHistory,
                                onSelectHistory = { history ->
                                    val f = PhysicalUnit.find(history.fromUnitId)
                                    val t = PhysicalUnit.find(history.toUnitId)
                                    viewModel.setUnitCategory(f.category)
                                    viewModel.setUnitFrom(f)
                                    viewModel.setUnitTo(t)
                                },
                                onClearHistory = { viewModel.clearUnitHistory() }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                AnimatedVisibility(visible = showKeypad) {
                    CalculatorKeypad(
                        onKeyClick = { viewModel.onUnitKeyInput(it) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitCategorySelector(
    selectedCategory: UnitCategory,
    onSelectCategory: (UnitCategory) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unit_category_selector"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .dpadFocusable(shape = RoundedCornerShape(14.dp))
                        .clickable { onSelectCategory(category) }
                        .testTag("unit_tab_${category.name.lowercase()}"),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceUnitCard(
    unit: PhysicalUnit,
    expressionInput: String,
    evaluatedAmount: Double,
    onOpenPicker: () -> Unit,
    onToggleKeypad: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("source_unit_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
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
                        .clickable { onOpenPicker() }
                        .testTag("source_unit_selector"),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(unit.icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${unit.name} (${unit.symbol})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("▼", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                IconButton(
                    onClick = onToggleKeypad,
                    modifier = Modifier
                        .testTag("toggle_unit_keypad")
                        .dpadFocusable(shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keypad",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Input Expression
            AnimatedContent(
                targetState = if (expressionInput.isEmpty()) "0" else expressionInput,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "UnitExpressionAnimation"
            ) { targetText ->
                Text(
                    text = targetText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_input_expression_display")
                )
            }

            if (expressionInput.contains("+") || expressionInput.contains("-") ||
                expressionInput.contains("×") || expressionInput.contains("÷")
            ) {
                Text(
                    text = "= ${PhysicalUnit.formatUnitValue(evaluatedAmount)} ${unit.symbol}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SwapUnitsRow(onSwap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .dpadFocusable(shape = CircleShape)
                .clickable { onSwap() }
                .testTag("swap_units_btn"),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap Units",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TargetUnitCard(
    fromUnit: PhysicalUnit,
    toUnit: PhysicalUnit,
    convertedValue: Double,
    isFavorite: Boolean,
    onOpenPicker: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("target_unit_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
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
                        .clickable { onOpenPicker() }
                        .testTag("target_unit_selector"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(toUnit.icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${toUnit.name} (${toUnit.symbol})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("▼", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("favorite_unit_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite Unit Pair",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.testTag("copy_converted_unit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Value",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Formatted Result Display
            Text(
                text = "${PhysicalUnit.formatUnitValue(convertedValue)} ${toUnit.symbol}",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("converted_unit_value_display")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Conversion Formula Pill
            val unitFactor = PhysicalUnit.convert(1.0, fromUnit, toUnit)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "1 ${fromUnit.symbol} = ${PhysicalUnit.formatUnitValue(unitFactor)} ${toUnit.symbol}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun AllEquivalentsMatrixCard(
    category: UnitCategory,
    currentToUnitId: String,
    equivalents: Map<String, Double>,
    onSelectTargetUnit: (PhysicalUnit) -> Unit
) {
    val units = PhysicalUnit.getUnitsForCategory(category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("all_equivalents_matrix_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All ${category.displayName} Equivalents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tap to target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                units.forEach { unit ->
                    val isCurrentTarget = unit.id == currentToUnitId
                    val value = equivalents[unit.id] ?: 0.0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectTargetUnit(unit) }
                            .testTag("equivalent_item_${unit.id}"),
                        color = if (isCurrentTarget) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(unit.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = unit.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = unit.symbol,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${PhysicalUnit.formatUnitValue(value)} ${unit.symbol}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isCurrentTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitRecentHistoryCard(
    historyList: List<UnitConversionHistoryEntity>,
    onSelectHistory: (UnitConversionHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unit_recent_history_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Unit Conversions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (historyList.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_unit_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Unit History",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (historyList.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent unit conversions logged yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    historyList.take(6).forEach { history ->
                        val fromU = PhysicalUnit.find(history.fromUnitId)
                        val toU = PhysicalUnit.find(history.toUnitId)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectHistory(history) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${PhysicalUnit.formatUnitValue(history.fromValue)} ${fromU.symbol} ➔ ${PhysicalUnit.formatUnitValue(history.toValue)} ${toU.symbol}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = sdf.format(Date(history.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "${fromU.icon} ➔ ${toU.icon}",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitSelectionSheet(
    title: String,
    units: List<PhysicalUnit>,
    onUnitSelected: (PhysicalUnit) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredUnits = remember(searchQuery, units) {
        if (searchQuery.isBlank()) units
        else units.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unit_search_input"),
                placeholder = { Text("Search unit by name or symbol...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUnits, key = { it.id }) { unit ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUnitSelected(unit) }
                            .testTag("select_unit_${unit.id}"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(unit.icon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = unit.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = unit.category.displayName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = unit.symbol,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Unit Value", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
}
