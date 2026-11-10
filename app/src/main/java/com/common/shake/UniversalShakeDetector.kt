package com.common.shake

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class UniversalShakeDetector(
    private val onShakeTriggered: () -> Unit
) : SensorEventListener {

    // Threshold in units of g (1.0 = normal Earth gravity ~9.81 m/s²)
    var thresholdGForce: Float = 2.5f
    var isEnabled: Boolean = true

    private var lastShakeTimestamp: Long = 0L
    private val debounceSlopMs: Long = 1000L // Prevent rapid multiple triggers

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Convert acceleration to g-force magnitude
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH
        val gForceMagnitude = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForceMagnitude >= thresholdGForce) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > debounceSlopMs) {
                lastShakeTimestamp = now
                onShakeTriggered()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
