package com.common.engagement

import android.content.Context
import android.content.SharedPreferences

data class EngagementState(
    val lastPromptTimestamp: Long = 0L,
    val installTimestamp: Long = 0L,
    val launchCount: Int = 0,
    val successfulTasksCount: Int = 0,
    val crashCount: Int = 0,
    val isRated: Boolean = false,
    val lastRatingPromptTime: Long = 0L,
    val ratingPromptCount: Int = 0,
    val isShared: Boolean = false,
    val lastSharePromptTime: Long = 0L,
    val sharePromptCount: Int = 0,
    val lastUpdatePromptTime: Long = 0L
)

enum class PromptType {
    NONE,
    CRITICAL_UPDATE,
    FLEXIBLE_UPDATE,
    RATE_APP,
    SHARE_APP
}

class EngagementTrafficController(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_engagement_engine_prefs", Context.MODE_PRIVATE)

    companion object {
        const val GLOBAL_COOLDOWN_MS = 48 * 60 * 60 * 1000L // 48 Hours
        const val RATING_COOLDOWN_MS = 30L * 24 * 60 * 60 * 1000L // 30 Days
        const val SHARE_COOLDOWN_MS = 21L * 24 * 60 * 60 * 1000L // 21 Days
    }

    init {
        // Record first install timestamp if absent
        if (!prefs.contains("install_timestamp")) {
            prefs.edit().putLong("install_timestamp", System.currentTimeMillis()).apply()
        }
    }

    fun evaluateNextEligiblePrompt(
        now: Long = System.currentTimeMillis(),
        hasPendingUpdate: Boolean = false,
        isUpdateMandatory: Boolean = false,
        isCriticalFlowActive: Boolean = false
    ): PromptType {
        // 1. Guard check: Suppress if user is inside a critical flow or emergency task
        if (isCriticalFlowActive) return PromptType.NONE

        // 2. Critical updates bypass standard 48-hour global cooldown
        if (hasPendingUpdate && isUpdateMandatory) {
            return PromptType.CRITICAL_UPDATE
        }

        // 3. Global Cooldown Check (Traffic Controller)
        val lastPrompt = prefs.getLong("last_any_prompt_timestamp", 0L)
        if (now - lastPrompt < GLOBAL_COOLDOWN_MS) return PromptType.NONE

        // 4. Flexible Update (highest priority after critical)
        if (hasPendingUpdate) {
            return PromptType.FLEXIBLE_UPDATE
        }

        // 5. In-App Review Check
        if (isEligibleForRating(now)) {
            return PromptType.RATE_APP
        }

        // 6. Share Prompt Check
        if (isEligibleForSharing(now)) {
            return PromptType.SHARE_APP
        }

        return PromptType.NONE
    }

    private fun isEligibleForRating(now: Long): Boolean {
        val isRated = prefs.getBoolean("is_rated", false)
        if (isRated) return false

        val ratingPromptCount = prefs.getInt("rating_prompt_count", 0)
        if (ratingPromptCount >= 3) return false

        val lastRatingPrompt = prefs.getLong("last_rating_prompt_time", 0L)
        if (now - lastRatingPrompt < RATING_COOLDOWN_MS) return false

        val installTimestamp = prefs.getLong("install_timestamp", now)
        val daysInstalled = (now - installTimestamp) / (24 * 60 * 60 * 1000L)
        val launches = prefs.getInt("launch_count", 0)
        val tasks = prefs.getInt("successful_tasks", 0)
        val crashes = prefs.getInt("crash_count", 0)

        // Strict quality gates
        return daysInstalled >= 5 && launches >= 7 && tasks >= 15 && crashes == 0
    }

    private fun isEligibleForSharing(now: Long): Boolean {
        val isShared = prefs.getBoolean("is_shared", false)
        if (isShared) return false

        val sharePromptCount = prefs.getInt("share_prompt_count", 0)
        if (sharePromptCount >= 2) return false

        val lastSharePrompt = prefs.getLong("last_share_prompt_time", 0L)
        if (now - lastSharePrompt < SHARE_COOLDOWN_MS) return false

        val tasks = prefs.getInt("successful_tasks", 0)
        return tasks >= 5
    }

    fun recordPromptShown(type: PromptType, timestamp: Long = System.currentTimeMillis()) {
        val editor = prefs.edit().putLong("last_any_prompt_timestamp", timestamp)
        when (type) {
            PromptType.RATE_APP -> {
                editor.putLong("last_rating_prompt_time", timestamp)
                editor.putInt("rating_prompt_count", prefs.getInt("rating_prompt_count", 0) + 1)
            }
            PromptType.SHARE_APP -> {
                editor.putLong("last_share_prompt_time", timestamp)
                editor.putInt("share_prompt_count", prefs.getInt("share_prompt_count", 0) + 1)
            }
            PromptType.FLEXIBLE_UPDATE, PromptType.CRITICAL_UPDATE -> {
                editor.putLong("last_update_prompt_time", timestamp)
            }
            PromptType.NONE -> Unit
        }
        editor.apply()
    }

    fun recordSuccessfulTask() {
        val current = prefs.getInt("successful_tasks", 0)
        prefs.edit().putInt("successful_tasks", current + 1).apply()
    }

    fun recordAppLaunch() {
        val current = prefs.getInt("launch_count", 0)
        prefs.edit().putInt("launch_count", current + 1).apply()
    }

    fun recordCrash() {
        val current = prefs.getInt("crash_count", 0)
        prefs.edit().putInt("crash_count", current + 1).apply()
    }

    fun getSuccessfulTasksCount(): Int = prefs.getInt("successful_tasks", 0)
    fun getLaunchCount(): Int = prefs.getInt("launch_count", 0)
    fun isRated(): Boolean = prefs.getBoolean("is_rated", false)
    fun isShared(): Boolean = prefs.getBoolean("is_shared", false)

    fun markRatingCompleted() = prefs.edit().putBoolean("is_rated", true).apply()
    fun markShareCompleted() = prefs.edit().putBoolean("is_shared", true).apply()

    // Testing helper
    fun resetForTesting() {
        prefs.edit().clear().apply()
    }
}
