package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.Currency
import com.example.ui.theme.dpadFocusable

enum class CurrencyCategoryFilter {
    ALL, POPULAR, FIAT, CRYPTO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionSheet(
    title: String = "Select Currency",
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CurrencyCategoryFilter.ALL) }

    val filteredList = remember(searchQuery, selectedCategory) {
        Currency.ALL_CURRENCIES.filter { c ->
            val matchesQuery = c.code.contains(searchQuery, ignoreCase = true) ||
                    c.name.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                CurrencyCategoryFilter.ALL -> true
                CurrencyCategoryFilter.POPULAR -> c.isPopular
                CurrencyCategoryFilter.FIAT -> !c.isCrypto
                CurrencyCategoryFilter.CRYPTO -> c.isCrypto
            }
            matchesQuery && matchesCategory
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
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
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_currency_input"),
                placeholder = { Text("Search currency (e.g. EUR, BTC, Yen)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyCategoryFilter.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.name) },
                        modifier = Modifier
                            .testTag("filter_${cat.name}")
                            .dpadFocusable(shape = RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredList, key = { it.code }) { currency ->
                    CurrencyRowItem(
                        currency = currency,
                        onClick = {
                            onCurrencySelected(currency)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyRowItem(
    currency: Currency,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .dpadFocusable(shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("currency_item_${currency.code}"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currency.flagOrIcon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${currency.symbol})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category badge
            val badgeBg = if (currency.isCrypto) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
            val badgeText = if (currency.isCrypto) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (currency.isCrypto) "CRYPTO" else "FIAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }
        }
    }
}
