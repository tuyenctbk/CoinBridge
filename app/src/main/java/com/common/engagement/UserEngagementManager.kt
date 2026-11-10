package com.common.engagement

import android.content.Context
import android.content.SharedPreferences

class UserEngagementManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_engagement_manager_prefs"

        // Cooldown configuration: minimum 30 days between non-critical prompts
        const val MIN_PROMPT_COOLDOWN_MS = 30L * 24 * 60 * 60 * 1000L // 30 Days

        // Milestone & threshold configuration
        const val MIN_INSTALL_DAYS_FOR_PROMPT = 3
        const val MIN_LAUNCHES_FOR_PROMPT = 3
        const val MIN_MANUAL_REFRESHES_FOR_RATING = 5
        const val MIN_CONVERSIONS_FOR_RATING = 5
        const val MIN_CONVERSIONS_FOR_SHARING = 5
        const val MIN_OPERATIONS_FOR_SHARING = 5
        const val MAX_RATING_PROMPTS = 3
        const val MAX_SHARE_PROMPTS = 2

        @Volatile
        private var instance: UserEngagementManager? = null

        fun getInstance(context: Context): UserEngagementManager {
            return instance ?: synchronized(this) {
                instance ?: UserEngagementManager(context.applicationContext).also { instance = it }
            }
        }

        // SharedPreferences Keys
        private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
        private const val KEY_SUCCESSFUL_OPERATIONS = "successful_operations_count"
        private const val KEY_MANUAL_REFRESHES = "manual_refresh_count"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val KEY_IS_RATED = "is_rated"
        private const val KEY_IS_SHARED = "is_shared"
        private const val KEY_LAST_ANY_PROMPT_TIMESTAMP = "last_any_prompt_timestamp"
        private const val KEY_LAST_RATING_PROMPT_TIME = "last_rating_prompt_time"
        private const val KEY_RATING_PROMPT_COUNT = "rating_prompt_count"
        private const val KEY_LAST_SHARE_PROMPT_TIME = "last_share_prompt_time"
        private const val KEY_SHARE_PROMPT_COUNT = "share_prompt_count"
        private const val KEY_LAST_UPDATE_PROMPT_TIME = "last_update_prompt_time"
    }

    init {
        // Record install timestamp on first run if not present
        if (!prefs.contains(KEY_INSTALL_TIMESTAMP)) {
            prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
        }
    }

    // --- Core Operations Tracking ---

    fun recordSuccessfulOperation() {
        val current = prefs.getInt(KEY_SUCCESSFUL_OPERATIONS, 0)
        prefs.edit().putInt(KEY_SUCCESSFUL_OPERATIONS, current + 1).apply()
    }

    /**
     * Persist successful conversion count (currency or unit conversions)
     */
    fun recordSuccessfulConversion() {
        recordSuccessfulOperation()
    }

    fun recordManualRefresh() {
        val current = prefs.getInt(KEY_MANUAL_REFRESHES, 0)
        prefs.edit().putInt(KEY_MANUAL_REFRESHES, current + 1).apply()
    }

    fun recordAppLaunch() {
        val current = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        prefs.edit().putInt(KEY_LAUNCH_COUNT, current + 1).apply()
    }

    fun recordCrash() {
        val current = prefs.getInt(KEY_CRASH_COUNT, 0)
        prefs.edit().putInt(KEY_CRASH_COUNT, current + 1).apply()
    }

    // --- Getters ---

    fun getInstallTimestamp(): Long = prefs.getLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis())

    fun getInstallDurationDays(now: Long = System.currentTimeMillis()): Int {
        val installTime = getInstallTimestamp()
        val durationMs = (now - installTime).coerceAtLeast(0L)
        return (durationMs / (24 * 60 * 60 * 1000L)).toInt()
    }

    fun getSuccessfulOperationsCount(): Int = prefs.getInt(KEY_SUCCESSFUL_OPERATIONS, 0)

    fun getSuccessfulConversionCount(): Int = getSuccessfulOperationsCount()

    fun getManualRefreshCount(): Int = prefs.getInt(KEY_MANUAL_REFRESHES, 0)

    fun getLaunchCount(): Int = prefs.getInt(KEY_LAUNCH_COUNT, 0)

    fun isRated(): Boolean = prefs.getBoolean(KEY_IS_RATED, false)

    fun isShared(): Boolean = prefs.getBoolean(KEY_IS_SHARED, false)

    fun getLastPromptTimestamp(): Long = prefs.getLong(KEY_LAST_ANY_PROMPT_TIMESTAMP, 0L)

    // --- Eligibility Decision Logic ---

    fun checkPromptEligibility(
        now: Long = System.currentTimeMillis(),
        hasPendingUpdate: Boolean = false,
        isUpdateMandatory: Boolean = false,
        isCriticalFlowActive: Boolean = false
    ): PromptType {
        // 1. Zero Disruption Rule: Suppress if user is inside a critical flow
        if (isCriticalFlowActive) return PromptType.NONE

        // 2. Critical updates bypass cooldown rules to ensure app safety
        if (hasPendingUpdate && isUpdateMandatory) {
            return PromptType.CRITICAL_UPDATE
        }

        // 3. Minimum 30-Day Cooldown Between Non-Critical Prompts
        val lastPrompt = getLastPromptTimestamp()
        if (lastPrompt > 0L && (now - lastPrompt) < MIN_PROMPT_COOLDOWN_MS) {
            return PromptType.NONE
        }

        // 4. Update Priority (Flexible Update)
        if (hasPendingUpdate) {
            val daysInstalled = getInstallDurationDays(now)
            if (daysInstalled >= MIN_INSTALL_DAYS_FOR_PROMPT) {
                return PromptType.FLEXIBLE_UPDATE
            }
        }

        // 5. In-App Review / Rating Eligibility (e.g. 5th manual refresh, 3 days post-install)
        if (isEligibleForRating(now)) {
            return PromptType.RATE_APP
        }

        // 6. Referral / Share App Eligibility
        if (isEligibleForSharing(now)) {
            return PromptType.SHARE_APP
        }

        return PromptType.NONE
    }

    fun isReviewPromptEligible(now: Long = System.currentTimeMillis()): Boolean = isEligibleForRating(now)

    fun isSharePromptEligible(now: Long = System.currentTimeMillis()): Boolean = isEligibleForSharing(now)

    private fun isEligibleForRating(now: Long): Boolean {
        if (isRated()) return false

        val ratingPromptCount = prefs.getInt(KEY_RATING_PROMPT_COUNT, 0)
        if (ratingPromptCount >= MAX_RATING_PROMPTS) return false

        val daysInstalled = getInstallDurationDays(now)
        val manualRefreshes = getManualRefreshCount()
        val conversions = getSuccessfulConversionCount()
        val crashes = prefs.getInt(KEY_CRASH_COUNT, 0)

        // Criteria: 3+ days post-install, milestone met (5+ manual refreshes OR 5+ successful conversions), zero crashes
        return daysInstalled >= MIN_INSTALL_DAYS_FOR_PROMPT &&
                (manualRefreshes >= MIN_MANUAL_REFRESHES_FOR_RATING || conversions >= MIN_CONVERSIONS_FOR_RATING) &&
                crashes == 0
    }

    private fun isEligibleForSharing(now: Long): Boolean {
        if (isShared()) return false

        val sharePromptCount = prefs.getInt(KEY_SHARE_PROMPT_COUNT, 0)
        if (sharePromptCount >= MAX_SHARE_PROMPTS) return false

        val daysInstalled = getInstallDurationDays(now)
        val operations = getSuccessfulOperationsCount()
        val conversions = getSuccessfulConversionCount()

        // Criteria: 3+ days post-install, 5+ operations/conversions
        return daysInstalled >= MIN_INSTALL_DAYS_FOR_PROMPT &&
                (operations >= MIN_OPERATIONS_FOR_SHARING || conversions >= MIN_CONVERSIONS_FOR_SHARING)
    }

    // --- State Recording & Completion Handlers ---

    fun recordPromptShown(type: PromptType, timestamp: Long = System.currentTimeMillis()) {
        val editor = prefs.edit().putLong(KEY_LAST_ANY_PROMPT_TIMESTAMP, timestamp)
        when (type) {
            PromptType.RATE_APP -> {
                editor.putLong(KEY_LAST_RATING_PROMPT_TIME, timestamp)
                editor.putInt(KEY_RATING_PROMPT_COUNT, prefs.getInt(KEY_RATING_PROMPT_COUNT, 0) + 1)
            }
            PromptType.SHARE_APP -> {
                editor.putLong(KEY_LAST_SHARE_PROMPT_TIME, timestamp)
                editor.putInt(KEY_SHARE_PROMPT_COUNT, prefs.getInt(KEY_SHARE_PROMPT_COUNT, 0) + 1)
            }
            PromptType.FLEXIBLE_UPDATE, PromptType.CRITICAL_UPDATE -> {
                editor.putLong(KEY_LAST_UPDATE_PROMPT_TIME, timestamp)
            }
            PromptType.NONE -> Unit
        }
        editor.apply()
    }

    fun markRatingCompleted() {
        prefs.edit().putBoolean(KEY_IS_RATED, true).apply()
    }

    fun markReviewCompleted() {
        markRatingCompleted()
    }

    fun markShareCompleted() {
        prefs.edit().putBoolean(KEY_IS_SHARED, true).apply()
    }

    fun setInstallTimestampForTesting(timestamp: Long) {
        prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, timestamp).apply()
    }

    fun resetForTesting() {
        prefs.edit().clear().apply()
    }
}
