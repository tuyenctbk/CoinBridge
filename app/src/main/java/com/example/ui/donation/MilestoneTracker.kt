package com.example.ui.donation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Milestone(
    val count: Int,
    val title: String,
    val description: String,
    val badgeIconText: String
)

object MilestoneTracker {
    private const val TAG = "MilestoneTracker"
    private const val PREFS_NAME = "coinbridge_milestone_prefs"
    private const val KEY_LAUNCH_COUNT = "key_launch_count"
    private const val KEY_CONVERSION_COUNT = "key_conversion_count"
    private const val KEY_DISMISSED_MILESTONES = "key_dismissed_milestones"

    val MILESTONES = listOf(
        Milestone(
            count = 10,
            title = "10 Tasks Completed! 🎉",
            description = "You've completed 10 successful currency operations with CoinBridge. If this app is saving you time, consider supporting independent development!",
            badgeIconText = "10"
        ),
        Milestone(
            count = 25,
            title = "25 Tasks Milestone! 🌟",
            description = "Quarter-century landmark! You've completed 25 tasks with CoinBridge. Help us stay 100% ad-free and privacy-focused by supporting the developer.",
            badgeIconText = "25"
        ),
        Milestone(
            count = 50,
            title = "50 Tasks Power User! 🏆",
            description = "You are officially a CoinBridge Power User with 50 completed tasks! Consider leaving a tip to keep future features coming.",
            badgeIconText = "50"
        )
    )

    private val _launchCount = MutableStateFlow(0)
    val launchCount: StateFlow<Int> = _launchCount.asStateFlow()

    private val _conversionCount = MutableStateFlow(0)
    val conversionCount: StateFlow<Int> = _conversionCount.asStateFlow()

    private val _totalTaskCount = MutableStateFlow(0)
    val totalTaskCount: StateFlow<Int> = _totalTaskCount.asStateFlow()

    private val _activeMilestone = MutableStateFlow<Milestone?>(null)
    val activeMilestone: StateFlow<Milestone?> = _activeMilestone.asStateFlow()

    private val dismissedMilestones = mutableSetOf<Int>()

    fun init(context: Context) {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _launchCount.value = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        _conversionCount.value = prefs.getInt(KEY_CONVERSION_COUNT, 0)
        _totalTaskCount.value = _launchCount.value + _conversionCount.value

        val dismissedString = prefs.getStringSet(KEY_DISMISSED_MILESTONES, emptySet()) ?: emptySet()
        dismissedMilestones.clear()
        dismissedMilestones.addAll(dismissedString.mapNotNull { it.toIntOrNull() })
    }

    fun recordAppLaunch(context: Context) {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newLaunchCount = _launchCount.value + 1
        _launchCount.value = newLaunchCount
        prefs.edit().putInt(KEY_LAUNCH_COUNT, newLaunchCount).apply()

        updateTotalTasks(appCtx)
    }

    fun recordConversionSuccess(context: Context) {
        val appCtx = context.applicationContext
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newConvCount = _conversionCount.value + 1
        _conversionCount.value = newConvCount
        prefs.edit().putInt(KEY_CONVERSION_COUNT, newConvCount).apply()

        // Also notify DonationManager
        DonationManager.recordOperationSuccess(appCtx)

        updateTotalTasks(appCtx)
    }

    private fun updateTotalTasks(context: Context) {
        val total = _conversionCount.value
        _totalTaskCount.value = total

        // Check if user is already a supporter
        if (DonationManager.isSupporter.value) {
            _activeMilestone.value = null
            return
        }

        // Check if current total reaches any milestone not yet dismissed
        val milestone = MILESTONES.find { m ->
            total >= m.count && !dismissedMilestones.contains(m.count)
        }

        if (milestone != null) {
            _activeMilestone.value = milestone
        }
    }

    fun dismissActiveMilestone(context: Context) {
        val current = _activeMilestone.value
        if (current != null) {
            dismissedMilestones.add(current.count)
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putStringSet(KEY_DISMISSED_MILESTONES, dismissedMilestones.map { it.toString() }.toSet())
                .apply()
        }
        _activeMilestone.value = null
    }

    fun resetForTesting(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _launchCount.value = 0
        _conversionCount.value = 0
        _totalTaskCount.value = 0
        _activeMilestone.value = null
        dismissedMilestones.clear()
    }
}
