package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.common.shake.ui.ShakeSensitivitySettingCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.local.ConversionHistoryEntity
import com.example.data.local.FavoritePairEntity
import com.example.data.local.FavoriteUnitPairEntity
import com.example.data.local.UnitConversionHistoryEntity
import com.example.data.model.Currency
import com.example.data.model.PhysicalUnit
import com.example.ui.calculator.ExpressionEvaluator.toCleanString
import com.example.ui.donation.DonationManager
import com.example.ui.security.BiometricAuthManager
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.viewmodel.CoinBridgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesHistoryScreen(
    viewModel: CoinBridgeViewModel,
    onOpenDonationSheet: () -> Unit = {},
    onOpenSupportScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val favoritePairs by viewModel.favoritePairs.collectAsState()
    val historyList by viewModel.conversionHistory.collectAsState()
    val favoriteUnits by viewModel.favoriteUnits.collectAsState()
    val unitHistoryList by viewModel.unitConversionHistory.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val lastSync by viewModel.lastSyncTimestamp.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()
    val biometricStatus by viewModel.biometricStatus.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()
    val shakeThresholdGForce by viewModel.shakeThresholdGForce.collectAsState()

    val isSupporter by DonationManager.isSupporter.collectAsState()
    val supporterTier by DonationManager.supporterTier.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val lastSyncFormatted = if (lastSync > 0) sdf.format(Date(lastSync)) else "Offline Cached"

    fun requestClearHistory() {
        if (isBiometricLockEnabled && activity != null) {
            BiometricAuthManager.promptBiometric(
                activity = activity,
                title = "Confirm History Deletion",
                subtitle = "Authenticate to permanently clear conversion history",
                description = "Verification required to protect sensitive financial records",
                onSuccess = {
                    viewModel.clearHistory()
                    viewModel.clearUnitHistory()
                    Toast.makeText(context, "All conversion history cleared", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(context, "Authentication error: $err", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            showClearConfirmDialog = true
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All Conversion History?") },
            text = { Text("This will permanently remove all currency and unit conversion log records from your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        viewModel.clearUnitHistory()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_btn")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val isWideScreen = maxWidth >= 640.dp

        if (isWideScreen) {
            // Tablet / Desktop 2-Column Responsive Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Status, Biometric Security, Support Section & Saved Pairs
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SyncStatusCard(
                            isOffline = isOffline,
                            lastSyncFormatted = lastSyncFormatted,
                            onSync = { viewModel.refreshRates() }
                        )
                    }

                    item {
                        BiometricSecurityCard(
                            isBiometricEnabled = isBiometricLockEnabled,
                            biometricStatusDescription = biometricStatus.description,
                            canAuthenticate = biometricStatus.canAuthenticate,
                            onToggle = { enabled ->
                                if (enabled && activity != null) {
                                    BiometricAuthManager.promptBiometric(
                                        activity = activity,
                                        title = "Enable App Lock",
                                        subtitle = "Verify biometrics to enable CoinBridge privacy lock",
                                        onSuccess = {
                                            viewModel.setBiometricLockEnabled(true)
                                            Toast.makeText(context, "Biometric lock enabled", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, "Could not verify: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    viewModel.setBiometricLockEnabled(enabled)
                                }
                            },
                            onLockNow = {
                                viewModel.lockApp()
                            }
                        )
                    }

                    // Device Shake Sensitivity Controller
                    item {
                        ShakeSensitivitySettingCard(
                            isEnabled = isShakeEnabled,
                            thresholdGForce = shakeThresholdGForce,
                            onEnabledChanged = { viewModel.setShakeEnabled(it) },
                            onThresholdChanged = { viewModel.setShakeThresholdGForce(it) }
                        )
                    }

                    // Static Permanent Support Development Section
                    item {
                        SupportDevelopmentCard(
                            isSupporter = isSupporter,
                            supporterTier = supporterTier,
                            onOpenSupportScreen = onOpenSupportScreen,
                            onRateApp = { viewModel.triggerManualRating() },
                            onShareApp = { viewModel.triggerManualShare() }
                        )
                    }

                    item {
                        SectionHeader(icon = Icons.Default.Star, title = "Saved Currency Pairs")
                    }

                    if (favoritePairs.isEmpty()) {
                        item { EmptyCard(text = "No saved favorite currency pairs yet. Tap the star icon on any pair to save.") }
                    } else {
                        items(favoritePairs, key = { it.id }) { fav ->
                            FavoritePairItem(
                                favorite = fav,
                                onSelect = {
                                    viewModel.setBaseCurrency(Currency.find(fav.fromCode))
                                    viewModel.addTargetCurrency(Currency.find(fav.toCode))
                                },
                                onDelete = { viewModel.toggleFavoritePair(fav.fromCode, fav.toCode) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(icon = Icons.Default.Straighten, title = "Saved Unit Pairs")
                    }

                    if (favoriteUnits.isEmpty()) {
                        item { EmptyCard(text = "No saved unit pairs yet. Tap the star icon on any unit conversion to save.") }
                    } else {
                        items(favoriteUnits, key = { "${it.category}_${it.fromUnitId}_${it.toUnitId}" }) { fav ->
                            FavoriteUnitItem(
                                favorite = fav,
                                onSelect = {
                                    val f = PhysicalUnit.find(fav.fromUnitId)
                                    val t = PhysicalUnit.find(fav.toUnitId)
                                    viewModel.setUnitCategory(f.category)
                                    viewModel.setUnitFrom(f)
                                    viewModel.setUnitTo(t)
                                },
                                onDelete = { viewModel.toggleFavoriteUnit(fav.fromUnitId, fav.toUnitId) }
                            )
                        }
                    }
                }

                // Right Column: Conversion History Logs
                LazyColumn(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(icon = Icons.Default.History, title = "Currency Conversion Logs")
                            if (historyList.isNotEmpty() || unitHistoryList.isNotEmpty()) {
                                IconButton(
                                    onClick = { requestClearHistory() },
                                    modifier = Modifier.testTag("clear_history_btn")
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    if (historyList.isEmpty()) {
                        item { EmptyCard(text = "No currency conversions logged yet.") }
                    } else {
                        items(historyList, key = { it.id }) { item ->
                            HistoryRowItem(history = item, sdf = sdf)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(icon = Icons.Default.Straighten, title = "Unit Conversion Logs")
                    }

                    if (unitHistoryList.isEmpty()) {
                        item { EmptyCard(text = "No unit conversions logged yet.") }
                    } else {
                        items(unitHistoryList, key = { it.id }) { item ->
                            UnitHistoryRowItem(history = item, sdf = sdf)
                        }
                    }
                }
            }
        } else {
            // Compact Phone Vertical Layout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Sync & Cache Status
                item {
                    SyncStatusCard(
                        isOffline = isOffline,
                        lastSyncFormatted = lastSyncFormatted,
                        onSync = { viewModel.refreshRates() }
                    )
                }

                // 2. Biometric Security Privacy Card
                item {
                    BiometricSecurityCard(
                        isBiometricEnabled = isBiometricLockEnabled,
                        biometricStatusDescription = biometricStatus.description,
                        canAuthenticate = biometricStatus.canAuthenticate,
                        onToggle = { enabled ->
                            if (enabled && activity != null) {
                                BiometricAuthManager.promptBiometric(
                                    activity = activity,
                                    title = "Enable App Lock",
                                    subtitle = "Verify biometrics to enable CoinBridge privacy lock",
                                    onSuccess = {
                                        viewModel.setBiometricLockEnabled(true)
                                        Toast.makeText(context, "Biometric lock enabled", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Could not verify: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.setBiometricLockEnabled(enabled)
                            }
                        },
                        onLockNow = {
                            viewModel.lockApp()
                        }
                    )
                }

                // 2b. Device Shake Gesture Control Card
                item {
                    ShakeSensitivitySettingCard(
                        isEnabled = isShakeEnabled,
                        thresholdGForce = shakeThresholdGForce,
                        onEnabledChanged = { viewModel.setShakeEnabled(it) },
                        onThresholdChanged = { viewModel.setShakeThresholdGForce(it) }
                    )
                }

                // 3. Static Permanent Support Development Section in Settings
                item {
                    SupportDevelopmentCard(
                        isSupporter = isSupporter,
                        supporterTier = supporterTier,
                        onOpenSupportScreen = onOpenSupportScreen,
                        onRateApp = { viewModel.triggerManualRating() },
                        onShareApp = { viewModel.triggerManualShare() }
                    )
                }

                // 4. Favorite Pairs Section
                item {
                    SectionHeader(icon = Icons.Default.Star, title = "Saved Currency Pairs")
                }

                if (favoritePairs.isEmpty()) {
                    item { EmptyCard(text = "No saved favorite currency pairs yet. Tap the star icon on any pair to save.") }
                } else {
                    items(favoritePairs, key = { it.id }) { fav ->
                        FavoritePairItem(
                            favorite = fav,
                            onSelect = {
                                viewModel.setBaseCurrency(Currency.find(fav.fromCode))
                                viewModel.addTargetCurrency(Currency.find(fav.toCode))
                            },
                            onDelete = { viewModel.toggleFavoritePair(fav.fromCode, fav.toCode) }
                        )
                    }
                }

                // 4b. Favorite Unit Pairs Section
                item {
                    SectionHeader(icon = Icons.Default.Straighten, title = "Saved Unit Pairs")
                }

                if (favoriteUnits.isEmpty()) {
                    item { EmptyCard(text = "No saved unit pairs yet. Tap the star icon on any unit conversion to save.") }
                } else {
                    items(favoriteUnits, key = { "${it.category}_${it.fromUnitId}_${it.toUnitId}" }) { fav ->
                        FavoriteUnitItem(
                            favorite = fav,
                            onSelect = {
                                val f = PhysicalUnit.find(fav.fromUnitId)
                                val t = PhysicalUnit.find(fav.toUnitId)
                                viewModel.setUnitCategory(f.category)
                                viewModel.setUnitFrom(f)
                                viewModel.setUnitTo(t)
                            },
                            onDelete = { viewModel.toggleFavoriteUnit(fav.fromUnitId, fav.toUnitId) }
                        )
                    }
                }

                // 5. Conversion History Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(icon = Icons.Default.History, title = "Currency Conversion Logs")
                        if (historyList.isNotEmpty() || unitHistoryList.isNotEmpty()) {
                            IconButton(
                                onClick = { requestClearHistory() },
                                modifier = Modifier.testTag("clear_history_btn")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (historyList.isEmpty()) {
                    item { EmptyCard(text = "No currency conversions logged yet.") }
                } else {
                    items(historyList, key = { it.id }) { item ->
                        HistoryRowItem(history = item, sdf = sdf)
                    }
                }

                // 5b. Unit Conversion History Section
                item {
                    SectionHeader(icon = Icons.Default.Straighten, title = "Unit Conversion Logs")
                }

                if (unitHistoryList.isEmpty()) {
                    item { EmptyCard(text = "No unit conversions logged yet.") }
                } else {
                    items(unitHistoryList, key = { it.id }) { item ->
                        UnitHistoryRowItem(history = item, sdf = sdf)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SupportDevelopmentCard(
    isSupporter: Boolean,
    supporterTier: String,
    onOpenSupportScreen: () -> Unit,
    onRateApp: () -> Unit = {},
    onShareApp: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("support_development_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSupporter) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSupporter) Amber400.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (isSupporter) Amber400 else Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSupporter) "Active Supporter ($supporterTier)" else "Support Independent Development",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "100% Ad-Free • Zero Trackers • Offline-First",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isSupporter) {
                    "Thank you for backing CoinBridge! Your voluntary contribution ensures this utility remains completely ad-free, private, and subscription-free for everyone."
                } else {
                    "CoinBridge is built with no third-party ads or analytics tracking. If our fast converter and travel bill tools have helped you, voluntary tips help maintain the project!"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenSupportScreen,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSupporter) MaterialTheme.colorScheme.primary else Emerald500
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_support_dialog_btn")
            ) {
                Icon(
                    imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSupporter) "View Supporter Perks & Options" else "Support Development / Tip ☕",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Community & Feedback Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRateApp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_app_settings_btn")
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rate App", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShareApp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_app_settings_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyCard(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyncStatusCard(
    isOffline: Boolean,
    lastSyncFormatted: String,
    onSync: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offline_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOffline) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.CloudDone,
                        contentDescription = "Sync state",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOffline) "Offline Cache Mode" else "Live Rates Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last synced: $lastSyncFormatted",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onSync,
                modifier = Modifier.testTag("force_sync_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync")
            }
        }
    }
}

@Composable
private fun BiometricSecurityCard(
    isBiometricEnabled: Boolean,
    biometricStatusDescription: String,
    canAuthenticate: Boolean,
    onToggle: (Boolean) -> Unit,
    onLockNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("biometric_security_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Security",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Privacy & Biometric Lock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Secure financial logs & settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { onToggle(it) },
                    modifier = Modifier.testTag("biometric_toggle_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hardware Status Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (canAuthenticate) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (canAuthenticate) Emerald500 else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = biometricStatusDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isBiometricEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onLockNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lock_app_now_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock App Now")
                }
            }
        }
    }
}

@Composable
private fun FavoritePairItem(
    favorite: FavoritePairEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val fromCurr = Currency.find(favorite.fromCode)
    val toCurr = Currency.find(favorite.toCode)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("fav_item_${favorite.fromCode}_${favorite.toCode}"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${fromCurr.flagOrIcon} ${fromCurr.code}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(" ➔ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${toCurr.flagOrIcon} ${toCurr.code}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Star, contentDescription = "Unstar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun HistoryRowItem(
    history: ConversionHistoryEntity,
    sdf: SimpleDateFormat
) {
    val fromCurr = Currency.find(history.fromCode)
    val toCurr = Currency.find(history.toCode)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${history.fromAmount.toCleanString()} ${fromCurr.code} ➔ ${history.toAmount.toCleanString()} ${toCurr.code}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sdf.format(Date(history.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${fromCurr.flagOrIcon} ➔ ${toCurr.flagOrIcon}",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun FavoriteUnitItem(
    favorite: FavoriteUnitPairEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val fromU = PhysicalUnit.find(favorite.fromUnitId)
    val toU = PhysicalUnit.find(favorite.toUnitId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("fav_unit_${favorite.fromUnitId}_${favorite.toUnitId}"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${fromU.icon} ${fromU.name} (${fromU.symbol})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(" ➔ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${toU.icon} ${toU.name} (${toU.symbol})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Star, contentDescription = "Unstar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun UnitHistoryRowItem(
    history: UnitConversionHistoryEntity,
    sdf: SimpleDateFormat
) {
    val fromU = PhysicalUnit.find(history.fromUnitId)
    val toU = PhysicalUnit.find(history.toUnitId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${PhysicalUnit.formatUnitValue(history.fromValue)} ${fromU.symbol} ➔ ${PhysicalUnit.formatUnitValue(history.toValue)} ${toU.symbol}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${history.category} • ${sdf.format(Date(history.timestamp))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${fromU.icon} ➔ ${toU.icon}",
                fontSize = 20.sp
            )
        }
    }
}

