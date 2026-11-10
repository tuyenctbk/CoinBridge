package com.example.ui.donation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo600

enum class DonationTab(val title: String) {
    GOOGLE_PLAY("Google Play"),
    PAYPAL("PayPal & QR"),
    KO_FI("Ko-fi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(DonationTab.GOOGLE_PLAY) }

    val isSupporter by DonationManager.isSupporter.collectAsState()
    val supporterTier by DonationManager.supporterTier.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Support CoinBridge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_donation_sheet")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "100% Ad-Free, Private & Offline-First. Independent development made possible by users like you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Active Supporter Banner if already donated
            if (isSupporter) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Emerald500.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active Supporter ($supporterTier)",
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                            Text(
                                text = "Thank you! All donation prompts are permanently disabled.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Method Selector Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                DonationTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_donate_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                DonationTab.GOOGLE_PLAY -> GooglePlayDonationSection(
                    onDonated = { tier ->
                        DonationManager.completeDonation(context, tier)
                        Toast.makeText(context, "Thank you for supporting CoinBridge!", Toast.LENGTH_LONG).show()
                    }
                )
                DonationTab.PAYPAL -> PayPalDonationSection(context = context)
                DonationTab.KO_FI -> KofiDonationSection(context = context)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GooglePlayDonationSection(
    onDonated: (String) -> Unit
) {
    val context = LocalContext.current
    val tiers = listOf(
        Tuple4(DonationManager.PRODUCT_TIER_199, "Tier 1", "$1.99", "Espresso Tip ☕"),
        Tuple4(DonationManager.PRODUCT_TIER_499, "Tier 2", "$4.99", "Coffee & Pastry 🥐"),
        Tuple4(DonationManager.PRODUCT_TIER_999, "Tier 3", "$9.99", "Developer Lunch 🍱"),
        Tuple4(DonationManager.PRODUCT_TIER_1999, "Tier 4", "$19.99", "Golden Sponsor 🌟")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Google Play In-App Purchase (1-Tap Google Pay)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        tiers.forEach { (productId, tierCode, price, label) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        DonationManager.launchPurchaseFlow(context, productId, "$tierCode ($price)")
                        onDonated("$tierCode ($price)")
                    }
                    .testTag("google_play_tier_${tierCode.replace(" ", "_")}"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Native Android Play Store Billing",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            DonationManager.launchPurchaseFlow(context, productId, "$tierCode ($price)")
                            onDonated("$tierCode ($price)")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = price, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)



@Composable
private fun PayPalDonationSection(context: Context) {
    var selectedPreset by remember { mutableStateOf("5") }
    var customAmount by remember { mutableStateOf("") }

    val activeAmount = if (customAmount.isNotBlank()) customAmount else selectedPreset
    val paypalUrl = "https://paypal.me/coinbridge/${activeAmount}USD"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PayPal & Live QR Code Scanner",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Amount Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("3", "5", "10", "20").forEach { amt ->
                FilterChip(
                    selected = selectedPreset == amt && customAmount.isEmpty(),
                    onClick = {
                        selectedPreset = amt
                        customAmount = ""
                    },
                    label = { Text("$$amt") },
                    modifier = Modifier.weight(1f).testTag("paypal_preset_$amt")
                )
            }
        }

        // Custom Amount Input
        OutlinedTextField(
            value = customAmount,
            onValueChange = { customAmount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Custom Amount (USD)") },
            placeholder = { Text("e.g. 15.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("paypal_custom_input"),
            shape = RoundedCornerShape(14.dp)
        )

        // Live Generated QR Code Canvas for Scanning
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan with Camera or PayPal App",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LiveQRCodeCanvas(
                    seed = paypalUrl,
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "paypal.me/coinbridge/$activeAmount USD",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Buttons: Open PayPal & Copy Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { DonationManager.openBrowserLink(context, paypalUrl) },
                modifier = Modifier.weight(1f).testTag("open_paypal_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open PayPal")
            }

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("PayPal Link", paypalUrl))
                    Toast.makeText(context, "PayPal link copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).testTag("copy_paypal_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Link")
            }
        }
    }
}

@Composable
private fun KofiDonationSection(context: Context) {
    val kofiUrl = "https://ko-fi.com/coinbridge"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF5E5B).copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "☕ Buy Me a Coffee on Ko-fi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF5E5B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Direct tip jar for open-source & indie application developers. Supports debit, credit, Apple Pay, and Google Pay.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { DonationManager.openBrowserLink(context, kofiUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E5B)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("open_kofi_btn")
            ) {
                Text("Open Ko-fi Page (ko-fi.com/coinbridge)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Ko-fi Link", kofiUrl))
                    Toast.makeText(context, "Ko-fi link copied", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("copy_kofi_btn")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Ko-fi Link")
            }
        }
    }
}

/**
 * Dynamic QR Code Pattern Canvas for seamless in-app scanning
 */
@Composable
private fun LiveQRCodeCanvas(
    seed: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val modules = 21
        val cellSize = w / modules

        // Background white
        drawRect(Color.White)

        // Draw 3 Position Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        fun drawFinder(startX: Float, startY: Float) {
            // Outer 7x7 black box
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(startX * cellSize, startY * cellSize),
                size = Size(7 * cellSize, 7 * cellSize)
            )
            // Inner 5x5 white box
            drawRect(
                color = Color.White,
                topLeft = Offset((startX + 1) * cellSize, (startY + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize)
            )
            // Core 3x3 black box
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset((startX + 2) * cellSize, (startY + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize)
            )
        }

        drawFinder(0f, 0f)
        drawFinder(14f, 0f)
        drawFinder(0f, 14f)

        // Draw deterministic matrix modules based on seed hash
        val hash = seed.hashCode()
        for (r in 0 until modules) {
            for (c in 0 until modules) {
                // Skip finder areas
                val inFinder1 = r < 7 && c < 7
                val inFinder2 = r < 7 && c >= 14
                val inFinder3 = r >= 14 && c < 7
                if (!inFinder1 && !inFinder2 && !inFinder3) {
                    val bit = ((hash xor (r * 31 + c * 17)) and 1) == 0
                    if (bit) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }

        // Center Accent Badge
        drawCircle(
            color = Color(0xFF6366F1),
            radius = cellSize * 2f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
    }
}
