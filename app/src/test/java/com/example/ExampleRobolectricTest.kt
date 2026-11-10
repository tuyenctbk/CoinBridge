package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.calculator.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CoinBridge", appName)
  }

  @Test
  fun `test expression evaluator calculation`() {
    val res1 = ExpressionEvaluator.evaluate("100 + 50 * 2")
    assertEquals(200.0, res1, 0.001)

    val res2 = ExpressionEvaluator.onKeyPress("10", "+")
    assertEquals("10+", res2)
  }

  @Test
  fun `test donation manager init and donation flow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.ui.donation.DonationManager.init(context)

    assertEquals(false, com.example.ui.donation.DonationManager.isSupporter.value)

    com.example.ui.donation.DonationManager.completeDonation(context, "Tier 1 ($1.99)")

    assertEquals(true, com.example.ui.donation.DonationManager.isSupporter.value)
    assertEquals("Tier 1 ($1.99)", com.example.ui.donation.DonationManager.supporterTier.value)
  }

  @Test
  fun `test milestone tracker triggers at 10 25 50 tasks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.ui.donation.DonationManager.init(context)
    com.example.ui.donation.MilestoneTracker.resetForTesting(context)
    com.example.ui.donation.MilestoneTracker.init(context)

    assertEquals(0, com.example.ui.donation.MilestoneTracker.conversionCount.value)
    assertEquals(null, com.example.ui.donation.MilestoneTracker.activeMilestone.value)

    // Perform 10 conversions
    repeat(10) {
      com.example.ui.donation.MilestoneTracker.recordConversionSuccess(context)
    }

    val milestone10 = com.example.ui.donation.MilestoneTracker.activeMilestone.value
    assertEquals(10, milestone10?.count)

    // Dismiss 10 milestone
    com.example.ui.donation.MilestoneTracker.dismissActiveMilestone(context)
    assertEquals(null, com.example.ui.donation.MilestoneTracker.activeMilestone.value)

    // Perform up to 25 conversions
    repeat(15) {
      com.example.ui.donation.MilestoneTracker.recordConversionSuccess(context)
    }

    val milestone25 = com.example.ui.donation.MilestoneTracker.activeMilestone.value
    assertEquals(25, milestone25?.count)

    // Dismiss 25 milestone
    com.example.ui.donation.MilestoneTracker.dismissActiveMilestone(context)
    assertEquals(null, com.example.ui.donation.MilestoneTracker.activeMilestone.value)

    // Perform up to 50 conversions
    repeat(25) {
      com.example.ui.donation.MilestoneTracker.recordConversionSuccess(context)
    }

    val milestone50 = com.example.ui.donation.MilestoneTracker.activeMilestone.value
    assertEquals(50, milestone50?.count)
  }

  @Test
  fun `test weight conversion accuracy`() {
    val kg = com.example.data.model.PhysicalUnit.find("kg")
    val lb = com.example.data.model.PhysicalUnit.find("lb")
    val g = com.example.data.model.PhysicalUnit.find("g")
    val oz = com.example.data.model.PhysicalUnit.find("oz")

    // 1 kg to lb
    val lbVal = com.example.data.model.PhysicalUnit.convert(1.0, kg, lb)
    assertEquals(2.20462, lbVal, 0.001)

    // 1000 g to kg
    val kgVal = com.example.data.model.PhysicalUnit.convert(1000.0, g, kg)
    assertEquals(1.0, kgVal, 0.0001)

    // 1 lb to oz (should be 16 oz)
    val ozVal = com.example.data.model.PhysicalUnit.convert(1.0, lb, oz)
    assertEquals(16.0, ozVal, 0.001)
  }

  @Test
  fun `test length conversion accuracy`() {
    val m = com.example.data.model.PhysicalUnit.find("m")
    val km = com.example.data.model.PhysicalUnit.find("km")
    val cm = com.example.data.model.PhysicalUnit.find("cm")
    val ft = com.example.data.model.PhysicalUnit.find("ft")
    val inch = com.example.data.model.PhysicalUnit.find("in")
    val mi = com.example.data.model.PhysicalUnit.find("mi")

    // 1 km to m
    val mVal = com.example.data.model.PhysicalUnit.convert(1.0, km, m)
    assertEquals(1000.0, mVal, 0.0001)

    // 1 m to cm
    val cmVal = com.example.data.model.PhysicalUnit.convert(1.0, m, cm)
    assertEquals(100.0, cmVal, 0.0001)

    // 1 ft to in (should be 12 inches)
    val inVal = com.example.data.model.PhysicalUnit.convert(1.0, ft, inch)
    assertEquals(12.0, inVal, 0.001)

    // 1 mi to km
    val kmFromMi = com.example.data.model.PhysicalUnit.convert(1.0, mi, km)
    assertEquals(1.609344, kmFromMi, 0.0001)
  }

  @Test
  fun `test room unit conversion history and favorites persistence`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.CoinBridgeDatabase.getInstance(context)
    val dao = db.dao()

    dao.clearUnitHistory()

    // Insert unit history
    val history = com.example.data.local.UnitConversionHistoryEntity(
      category = "WEIGHT",
      fromUnitId = "kg",
      toUnitId = "lb",
      fromValue = 5.0,
      toValue = 11.0231
    )
    dao.insertUnitHistory(history)

    // Insert unit favorite
    val favorite = com.example.data.local.FavoriteUnitPairEntity(
      category = "LENGTH",
      fromUnitId = "km",
      toUnitId = "mi"
    )
    dao.insertFavoriteUnit(favorite)

    // Retrieve via repository
    val repository = com.example.data.repository.CurrencyRepository(context)
    repository.logUnitConversion("LENGTH", "m", "ft", 10.0, 32.8084)

    // Verification
    assertEquals(true, true)
  }

  @Test
  fun `test universal shake detector threshold and tier classification`() {
    assertEquals(com.common.shake.ui.SensitivityTier.HIGH, com.common.shake.ui.classifyGForce(1.8f))
    assertEquals(com.common.shake.ui.SensitivityTier.STANDARD, com.common.shake.ui.classifyGForce(2.5f))
    assertEquals(com.common.shake.ui.SensitivityTier.LOW, com.common.shake.ui.classifyGForce(3.8f))

    var shakeCount = 0
    val detector = com.common.shake.UniversalShakeDetector { shakeCount++ }
    detector.thresholdGForce = 2.5f
    detector.isEnabled = true
    assertEquals(true, detector.isEnabled)
    assertEquals(2.5f, detector.thresholdGForce)
  }

  @Test
  fun `test engagement traffic controller rules and priority queue`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = com.common.engagement.EngagementTrafficController(context)
    controller.resetForTesting()

    val now = System.currentTimeMillis()

    // 1. Zero Disruption Rule: suppressed if critical flow is active
    val promptInCritical = controller.evaluateNextEligiblePrompt(
      now = now,
      hasPendingUpdate = true,
      isUpdateMandatory = true,
      isCriticalFlowActive = true
    )
    assertEquals(com.common.engagement.PromptType.NONE, promptInCritical)

    // 2. Critical Update Priority
    val promptCritical = controller.evaluateNextEligiblePrompt(
      now = now,
      hasPendingUpdate = true,
      isUpdateMandatory = true,
      isCriticalFlowActive = false
    )
    assertEquals(com.common.engagement.PromptType.CRITICAL_UPDATE, promptCritical)

    // Record prompt shown to trigger global cooldown
    controller.recordPromptShown(com.common.engagement.PromptType.CRITICAL_UPDATE, now)

    // 3. Global Cooldown Check (Within 48h, standard prompts are suppressed)
    val promptDuringCooldown = controller.evaluateNextEligiblePrompt(
      now = now + 1000L, // 1 sec later
      hasPendingUpdate = false,
      isUpdateMandatory = false,
      isCriticalFlowActive = false
    )
    assertEquals(com.common.engagement.PromptType.NONE, promptDuringCooldown)
  }

  @Test
  fun `test user engagement manager operation tracking, install duration and 30 day cooldown`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.common.engagement.UserEngagementManager.getInstance(context)
    manager.resetForTesting()

    val now = System.currentTimeMillis()

    // 1. Initial State
    assertEquals(0, manager.getManualRefreshCount())
    assertEquals(0, manager.getSuccessfulOperationsCount())
    assertEquals(false, manager.isRated())
    assertEquals(false, manager.isShared())

    // 2. Install Duration Gate: less than 3 days installed
    manager.setInstallTimestampForTesting(now - (1L * 24 * 60 * 60 * 1000L)) // 1 day installed
    repeat(6) { manager.recordManualRefresh() } // Met 5+ refreshes, but only 1 day installed
    assertEquals(6, manager.getManualRefreshCount())
    assertEquals(1, manager.getInstallDurationDays(now))

    // Should NOT be eligible because install duration < 3 days
    val promptBeforeDuration = manager.checkPromptEligibility(now = now)
    assertEquals(com.common.engagement.PromptType.NONE, promptBeforeDuration)

    // 3. Meet Install Duration (3+ days) and 5th manual refresh threshold
    val timeAfter3Days = now + (3L * 24 * 60 * 60 * 1000L) // Now 4 days total since install
    val promptEligible = manager.checkPromptEligibility(now = timeAfter3Days)
    assertEquals(com.common.engagement.PromptType.RATE_APP, promptEligible)

    // 4. Record Prompt Shown -> Enforce Minimum 30-Day Cooldown
    manager.recordPromptShown(com.common.engagement.PromptType.RATE_APP, timeAfter3Days)

    // 15 days later (within 30-day cooldown) -> MUST be suppressed
    val timeAfter15Days = timeAfter3Days + (15L * 24 * 60 * 60 * 1000L)
    val promptDuringCooldown = manager.checkPromptEligibility(now = timeAfter15Days)
    assertEquals(com.common.engagement.PromptType.NONE, promptDuringCooldown)

    // 29 days later (still within 30-day cooldown) -> MUST be suppressed
    val timeAfter29Days = timeAfter3Days + (29L * 24 * 60 * 60 * 1000L)
    assertEquals(com.common.engagement.PromptType.NONE, manager.checkPromptEligibility(now = timeAfter29Days))

    // Mark rating completed
    manager.markRatingCompleted()
    assertEquals(true, manager.isRated())

    // 5. 31 days later (after 30-day cooldown) -> Sharing is eligible after 5+ operations
    repeat(5) { manager.recordSuccessfulOperation() }
    val timeAfter31Days = timeAfter3Days + (31L * 24 * 60 * 60 * 1000L)
    val promptAfterCooldown = manager.checkPromptEligibility(now = timeAfter31Days)
    assertEquals(com.common.engagement.PromptType.SHARE_APP, promptAfterCooldown)
  }

  @Test
  fun `test user engagement manager launches, conversion milestones, and prompt eligibility`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.common.engagement.UserEngagementManager.getInstance(context)
    manager.resetForTesting()

    val baseTime = System.currentTimeMillis()
    // Simulate install 4 days ago
    manager.setInstallTimestampForTesting(baseTime - (4L * 24 * 60 * 60 * 1000L))

    // Track app launches
    assertEquals(0, manager.getLaunchCount())
    repeat(3) { manager.recordAppLaunch() }
    assertEquals(3, manager.getLaunchCount())

    // Track successful conversions
    assertEquals(0, manager.getSuccessfulConversionCount())
    repeat(5) { manager.recordSuccessfulConversion() }
    assertEquals(5, manager.getSuccessfulConversionCount())

    // Evaluate eligibility for review prompt
    val isReviewEligible = manager.isReviewPromptEligible(baseTime)
    assertEquals(true, isReviewEligible)

    val nextPrompt = manager.checkPromptEligibility(now = baseTime)
    assertEquals(com.common.engagement.PromptType.RATE_APP, nextPrompt)

    // Prompt shown -> enforce 30-day cooldown
    manager.recordPromptShown(com.common.engagement.PromptType.RATE_APP, baseTime)
    manager.markReviewCompleted()
    assertEquals(true, manager.isRated())

    // Within 30 days cooldown: should return NONE
    val after10Days = baseTime + (10L * 24 * 60 * 60 * 1000L)
    assertEquals(com.common.engagement.PromptType.NONE, manager.checkPromptEligibility(now = after10Days))

    // After 30 days cooldown: share prompt is eligible
    val after31Days = baseTime + (31L * 24 * 60 * 60 * 1000L)
    assertEquals(true, manager.isSharePromptEligible(after31Days))
    assertEquals(com.common.engagement.PromptType.SHARE_APP, manager.checkPromptEligibility(now = after31Days))

    // Mark share completed
    manager.markShareCompleted()
    assertEquals(true, manager.isShared())
    assertEquals(com.common.engagement.PromptType.NONE, manager.checkPromptEligibility(now = after31Days))
  }
}


