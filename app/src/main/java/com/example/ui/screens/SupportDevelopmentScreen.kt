package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.donation.DonationManager
import com.example.ui.theme.Amber400

data class DonationTierItem(
    val productId: String,
    val title: String,
    val price: String,
    val description: String,
    val icon: ImageVector,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDevelopmentScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current

    val isSupporter by DonationManager.isSupporter.collectAsState()
    val supporterTier by DonationManager.supporterTier.collectAsState()

    var selectedPaypalPreset by remember { mutableStateOf("5") }

    val playTiers = listOf(
        DonationTierItem(
            productId = DonationManager.PRODUCT_TIER_199,
            title = "Espresso Supporter",
            price = "$1.99",
            description = "Buys a quick shot of espresso to keep dev momentum going.",
            icon = Icons.Default.FreeBreakfast
        ),
        DonationTierItem(
            productId = DonationManager.PRODUCT_TIER_499,
            title = "Coffee & Donut",
            price = "$4.99",
            description = "Provides fuel for continuous maintenance and bug fixes.",
            icon = Icons.Default.Favorite,
            isPopular = true
        ),
        DonationTierItem(
            productId = DonationManager.PRODUCT_TIER_999,
            title = "Dev Fuel Pack",
            price = "$9.99",
            description = "Helps cover API servers and feature enhancements.",
            icon = Icons.Default.RocketLaunch
        ),
        DonationTierItem(
            productId = DonationManager.PRODUCT_TIER_1999,
            title = "Platinum Backer",
            price = "$19.99",
            description = "Sponsors independent ad-free mobile utility software.",
            icon = Icons.Default.Star
        )
    )

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser link", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.testTag("support_dev_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Support Development",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (isSupporter) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Amber400.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Amber400)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Amber400,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SUPPORTER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Amber400
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 640.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Hero Banner
                item {
                    HeroSupportCard(isSupporter = isSupporter, supporterTier = supporterTier)
                }

                // Section 1: Google Play In-App Billing
                item {
                    Text(
                        text = "Google Play 1-Tap Supporter Tiers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    if (isWideScreen) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PlayTierCard(
                                    tier = playTiers[0],
                                    modifier = Modifier.weight(1f),
                                    onSelect = {
                                        DonationManager.launchPurchaseFlow(context, playTiers[0].productId, playTiers[0].title)
                                    }
                                )
                                PlayTierCard(
                                    tier = playTiers[1],
                                    modifier = Modifier.weight(1f),
                                    onSelect = {
                                        DonationManager.launchPurchaseFlow(context, playTiers[1].productId, playTiers[1].title)
                                    }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PlayTierCard(
                                    tier = playTiers[2],
                                    modifier = Modifier.weight(1f),
                                    onSelect = {
                                        DonationManager.launchPurchaseFlow(context, playTiers[2].productId, playTiers[2].title)
                                    }
                                )
                                PlayTierCard(
                                    tier = playTiers[3],
                                    modifier = Modifier.weight(1f),
                                    onSelect = {
                                        DonationManager.launchPurchaseFlow(context, playTiers[3].productId, playTiers[3].title)
                                    }
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            playTiers.forEach { tier ->
                                PlayTierCard(
                                    tier = tier,
                                    modifier = Modifier.fillMaxWidth(),
                                    onSelect = {
                                        DonationManager.launchPurchaseFlow(context, tier.productId, tier.title)
                                    }
                                )
                            }
                        }
                    }
                }

                // Section 2: PayPal Direct & Links
                item {
                    Text(
                        text = "PayPal & Direct Tip Jar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    PayPalDonationCard(
                        selectedPreset = selectedPaypalPreset,
                        onPresetSelected = { selectedPaypalPreset = it },
                        onOpenPayPal = { amount ->
                            val url = "https://paypal.me/coinbridge/$amount"
                            openUrl(url)
                        },
                        onCopyLink = { amount ->
                            val url = "https://paypal.me/coinbridge/$amount"
                            copyToClipboard(url, "PayPal Link")
                        }
                    )
                }

                // Section 3: Web Platform (Ko-fi)
                item {
                    Text(
                        text = "Web Platform Tip Jar (Ko-fi)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    KofiDonationCard(
                        onOpenKofi = { openUrl("https://ko-fi.com/coinbridge") },
                        onCopyKofiLink = { copyToClipboard("https://ko-fi.com/coinbridge", "Ko-fi Page Link") }
                    )
                }

                // Section 4: Transparency FAQ
                item {
                    TransparencyFaqCard()
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun HeroSupportCard(
    isSupporter: Boolean,
    supporterTier: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("support_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSupporter) Amber400.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (isSupporter) Amber400 else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isSupporter) "Thank You, Supporter!" else "Support CoinBridge Development",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSupporter) "Active Tier: $supporterTier" else "Help keep our converter fast, private & ad-free",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CoinBridge is an open independent tool crafted for rapid travel currency calculations, bill splitting, and offline rates. We never display annoying popup ads or harvest personal financial data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Trust Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrustBadge(icon = Icons.Default.Security, label = "Ad-Free")
                TrustBadge(icon = Icons.Default.CheckCircle, label = "Zero Trackers")
                TrustBadge(icon = Icons.Default.RocketLaunch, label = "Offline-First")
            }
        }
    }
}

@Composable
private fun TrustBadge(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlayTierCard(
    tier: DonationTierItem,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSelect() }
            .testTag("donation_tier_${tier.productId.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tier.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tier.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = tier.price,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(text = "Tip ${tier.price} via Google Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PayPalDonationCard(
    selectedPreset: String,
    onPresetSelected: (String) -> Unit,
    onOpenPayPal: (String) -> Unit,
    onCopyLink: (String) -> Unit
) {
    val presets = listOf("3", "5", "10", "20")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paypal_donation_card"),
        shape = RoundedCornerShape(20.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = Color(0xFF003087),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PayPal Direct Transfer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant transfer via PayPal.me deep link",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Select Tip Amount (USD):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { amount ->
                    val isSelected = selectedPreset == amount
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetSelected(amount) },
                        label = { Text("$$amount") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paypal_preset_$amount"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenPayPal(selectedPreset) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_paypal_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open PayPal ($$selectedPreset)")
                }

                OutlinedButton(
                    onClick = { onCopyLink(selectedPreset) },
                    modifier = Modifier.testTag("copy_paypal_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun KofiDonationCard(
    onOpenKofi: () -> Unit,
    onCopyKofiLink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kofi_donation_card"),
        shape = RoundedCornerShape(20.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FreeBreakfast,
                    contentDescription = null,
                    tint = Color(0xFFFF5E5B),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ko-fi Tip Jar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Support via Ko-fi web page with zero platform fees",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenKofi,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_kofi_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5E5B),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Support on Ko-fi ☕")
                }

                OutlinedButton(
                    onClick = onCopyKofiLink,
                    modifier = Modifier.testTag("copy_kofi_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TransparencyFaqCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("faq_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FaqItem(
                question = "Are any features locked behind donations?",
                answer = "No! All core calculation, offline rate caching, biometric security, and bill splitting tools are 100% free for everyone forever."
            )

            Spacer(modifier = Modifier.height(8.dp))

            FaqItem(
                question = "What happens when I make a donation?",
                answer = "Your support is verified immediately and badges your app status as an Active Supporter. It directly helps maintain open exchange rate APIs and project updates!"
            )
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column {
        Text(
            text = question,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
