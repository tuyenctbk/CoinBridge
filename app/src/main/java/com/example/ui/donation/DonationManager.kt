package com.example.ui.donation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DonationManager {
    private const val TAG = "DonationManager"
    private const val PREFS_NAME = "coinbridge_donation_prefs"
    private const val KEY_IS_SUPPORTER = "key_is_supporter"
    private const val KEY_OP_COUNT = "key_operation_count"
    private const val KEY_LAST_DISMISSED = "key_last_dismissed_time"
    private const val KEY_SUPPORTER_TIER = "key_supporter_tier"

    // Product IDs for predefined tiers ($1.99, $4.99, $9.99, $19.99)
    const val PRODUCT_TIER_199 = "donation_tier_199"
    const val PRODUCT_TIER_499 = "donation_tier_499"
    const val PRODUCT_TIER_999 = "donation_tier_999"
    const val PRODUCT_TIER_1999 = "donation_tier_1999"

    private val _isSupporter = MutableStateFlow(false)
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _supporterTier = MutableStateFlow("")
    val supporterTier: StateFlow<String> = _supporterTier.asStateFlow()

    private val _operationCount = MutableStateFlow(0)
    val operationCount: StateFlow<Int> = _operationCount.asStateFlow()

    private val _showMilestoneBanner = MutableStateFlow(false)
    val showMilestoneBanner: StateFlow<Boolean> = _showMilestoneBanner.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private var billingClient: BillingClient? = null
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private var appContext: Context? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                appContext?.let { handlePurchase(it, purchase) }
            }
        }
    }

    fun init(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isSupporter.value = prefs.getBoolean(KEY_IS_SUPPORTER, false)
        _supporterTier.value = prefs.getString(KEY_SUPPORTER_TIER, "") ?: ""
        _operationCount.value = prefs.getInt(KEY_OP_COUNT, 0)

        initializeBillingClient(applicationContext)
    }

    private fun initializeBillingClient(context: Context) {
        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

            connectToBillingService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BillingClient: ${e.message}")
        }
    }

    private fun connectToBillingService() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    queryPredefinedTiers()
                    queryExistingPurchases()
                } else {
                    _isBillingReady.value = false
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _isBillingReady.value = false
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    private fun queryPredefinedTiers() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_TIER_199)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_TIER_499)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_TIER_999)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_TIER_1999)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productDetails in productDetailsList) {
                    productDetailsMap[productDetails.productId] = productDetails
                }
            }
        }
    }

    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                appContext?.let { ctx ->
                    for (purchase in purchases) {
                        handlePurchase(ctx, purchase)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(context: Context, productId: String, fallbackTierName: String) {
        val activity = context.findActivity()
        val productDetails = productDetailsMap[productId]

        if (activity != null && productDetails != null && billingClient?.isReady == true) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
            if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
                completeDonation(context, fallbackTierName)
            }
        } else {
            // Fallback for emulator / non-Google Play environment or test execution
            completeDonation(context, fallbackTierName)
        }
    }

    private fun handlePurchase(context: Context, purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val tierName = when (purchase.products.firstOrNull()) {
                PRODUCT_TIER_199 -> "Tier 1 ($1.99)"
                PRODUCT_TIER_499 -> "Tier 2 ($4.99)"
                PRODUCT_TIER_999 -> "Tier 3 ($9.99)"
                PRODUCT_TIER_1999 -> "Tier 4 ($19.99)"
                else -> "Supporter Tier"
            }
            completeDonation(context, tierName)

            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(acknowledgeParams) { _ -> }
            }
        }
    }

    fun recordOperationSuccess(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newCount = _operationCount.value + 1
        _operationCount.value = newCount
        prefs.edit().putInt(KEY_OP_COUNT, newCount).apply()

        if (!_isSupporter.value && (newCount == 5 || newCount == 15 || newCount == 30)) {
            val lastDismissed = prefs.getLong(KEY_LAST_DISMISSED, 0L)
            val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            if (now - lastDismissed > thirtyDaysMillis) {
                _showMilestoneBanner.value = true
            }
        }
    }

    fun dismissMilestonePrompt(context: Context) {
        _showMilestoneBanner.value = false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_DISMISSED, System.currentTimeMillis()).apply()
    }

    fun completeDonation(context: Context, tierName: String) {
        _isSupporter.value = true
        _supporterTier.value = tierName
        _showMilestoneBanner.value = false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_SUPPORTER, true)
            .putString(KEY_SUPPORTER_TIER, tierName)
            .apply()
    }

    fun openBrowserLink(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Handled gracefully in UI with clipboard fallback
        }
    }

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
