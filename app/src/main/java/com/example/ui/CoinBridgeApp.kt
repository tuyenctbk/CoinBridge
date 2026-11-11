package com.example.ui

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.engagement.PromptType
import com.common.engagement.ui.ShareAppDialog
import com.common.engagement.ui.TwoStepRatingDialog
import com.common.shake.UniversalShakeDetector
import com.example.ui.donation.DonationBottomSheet
import com.example.ui.donation.DonationManager
import com.example.ui.donation.MilestoneBottomSheet
import com.example.ui.donation.MilestoneTracker
import com.example.ui.screens.BillSplitterScreen
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.FavoritesHistoryScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SupportDevelopmentScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.dpadFocusable
import com.example.ui.viewmodel.CoinBridgeViewModel

import androidx.compose.ui.res.stringResource
import com.example.R

enum class CoinBridgeTab(
    val titleRes: Int,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    CONVERTER(R.string.tab_currency, Icons.Filled.CurrencyExchange, Icons.Outlined.CurrencyExchange),
    UNIT(R.string.tab_unit, Icons.Filled.Straighten, Icons.Outlined.Straighten),
    BILL_SPLITTER(R.string.tab_bill_splitter, Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    TRENDS(R.string.tab_rate_trends, Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    SAVED(R.string.tab_saved, Icons.Filled.Star, Icons.Outlined.Star);

    val title: String
        get() = when (this) {
            CONVERTER -> "Currency"
            UNIT -> "Unit"
            BILL_SPLITTER -> "Bill Splitter"
            TRENDS -> "Rate Trends"
            SAVED -> "Saved & Security"
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinBridgeApp(
    viewModel: CoinBridgeViewModel
) {
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(CoinBridgeTab.CONVERTER) }
    var showDonationSheet by remember { mutableStateOf(false) }
    var showSupportScreen by remember { mutableStateOf(false) }

    val isOffline by viewModel.isOffline.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()
    val isShakeEnabled by viewModel.isShakeEnabled.collectAsState()
    val shakeThreshold by viewModel.shakeThresholdGForce.collectAsState()
    val activeEngagementPrompt by viewModel.activeEngagementPrompt.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val targetCurrencies by viewModel.targetCurrencies.collectAsState()
    val isSupporter by DonationManager.isSupporter.collectAsState()
    val activeMilestone by MilestoneTracker.activeMilestone.collectAsState()

    androidx.compose.runtime.LaunchedEffect(currentTab) {
        viewModel.activeTab = currentTab.name
    }

    val context = LocalContext.current

    DisposableEffect(isShakeEnabled, shakeThreshold, isAppLocked) {
        if (!isShakeEnabled || isAppLocked) {
            return@DisposableEffect onDispose {}
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = UniversalShakeDetector {
            viewModel.onDeviceShaken()
            Toast.makeText(context, context.getString(R.string.shake_detected_toast), Toast.LENGTH_SHORT).show()
        }.apply {
            this.isEnabled = isShakeEnabled
            this.thresholdGForce = shakeThreshold
        }

        if (accelerometer != null) {
            sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(detector)
        }
    }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    if (isAppLocked) {
        BiometricLockScreen(onUnlocked = { viewModel.unlockApp() })
        return
    }

    if (showSupportScreen) {
        SupportDevelopmentScreen(onBack = { showSupportScreen = false })
        return
    }

    if (currentTab != CoinBridgeTab.CONVERTER) {
        BackHandler {
            currentTab = CoinBridgeTab.CONVERTER
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isOffline) Color(0xFFF59E0B) else Color(0xFF10B981))
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = if (isOffline) stringResource(R.string.status_offline).uppercase() else stringResource(R.string.status_online).uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                actions = {
                    // Support / Donation Heart Action
                    IconButton(
                        onClick = { showSupportScreen = true },
                        modifier = Modifier
                            .testTag("support_topbar_btn")
                            .dpadFocusable(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSupporter) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.support_development_title),
                            tint = if (isSupporter) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Lock Button if Biometric lock is enabled
                    if (isBiometricLockEnabled) {
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier
                                .testTag("lock_app_topbar")
                                .dpadFocusable(shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.lock_app_now),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Favorite Star Toggle
                    IconButton(
                        onClick = {
                            val target = targetCurrencies.firstOrNull()?.code ?: "EUR"
                            viewModel.toggleFavoritePair(baseCurrency.code, target)
                        },
                        modifier = Modifier
                            .testTag("star_favorite_topbar")
                            .dpadFocusable(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.tab_favorites),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshRates() },
                        modifier = Modifier
                            .testTag("refresh_rates_topbar")
                            .dpadFocusable(shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_sync),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation")
            ) {
                CoinBridgeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == tab) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = { Text(stringResource(tab.titleRes)) },
                        modifier = Modifier
                            .testTag("tab_${tab.name.lowercase()}")
                            .dpadFocusable(shape = RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 960.dp)
            ) {
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        CoinBridgeTab.CONVERTER -> ConverterScreen(
                            viewModel = viewModel,
                            onOpenDonationSheet = { showDonationSheet = true }
                        )
                        CoinBridgeTab.UNIT -> UnitConverterScreen(
                            viewModel = viewModel
                        )
                        CoinBridgeTab.BILL_SPLITTER -> BillSplitterScreen(viewModel = viewModel)
                        CoinBridgeTab.TRENDS -> TrendsScreen(viewModel = viewModel)
                        CoinBridgeTab.SAVED -> FavoritesHistoryScreen(
                            viewModel = viewModel,
                            onOpenDonationSheet = { showDonationSheet = true },
                            onOpenSupportScreen = { showSupportScreen = true }
                        )
                    }
                }
            }
        }
    }

    if (showDonationSheet) {
        DonationBottomSheet(
            onDismiss = { showDonationSheet = false }
        )
    }

    activeMilestone?.let { milestone ->
        MilestoneBottomSheet(
            milestone = milestone,
            onOpenDonationSheet = { showDonationSheet = true },
            onDismiss = { /* Handled inside MilestoneBottomSheet */ }
        )
    }

    // Engagement Traffic Controller Dialogs
    when (activeEngagementPrompt) {
        PromptType.RATE_APP -> {
            TwoStepRatingDialog(
                appName = "CoinBridge",
                onLaunchStoreReview = {
                    viewModel.markRatingCompleted()
                    val uri = Uri.parse("market://details?id=${context.packageName}")
                    val rateIntent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(rateIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Thank you for rating CoinBridge! ⭐⭐⭐⭐⭐", Toast.LENGTH_SHORT).show()
                    }
                },
                onRedirectInternalFeedback = {
                    viewModel.markRatingCompleted()
                    Toast.makeText(context, "Thank you! We'll keep improving CoinBridge.", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.dismissEngagementPrompt() }
            )
        }
        PromptType.SHARE_APP -> {
            ShareAppDialog(
                appName = "CoinBridge",
                onShare = {
                    viewModel.markShareCompleted()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "CoinBridge Currency & Unit Converter")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Check out CoinBridge: 100% ad-free, offline-ready currency and unit converter for Android!"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share CoinBridge"))
                },
                onDismiss = { viewModel.dismissEngagementPrompt() }
            )
        }
        else -> Unit
    }
}
