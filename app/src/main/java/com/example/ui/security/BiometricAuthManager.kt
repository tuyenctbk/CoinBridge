package com.example.ui.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability {
    AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED
}

data class BiometricStatus(
    val availability: BiometricAvailability,
    val description: String,
    val canAuthenticate: Boolean
)

object BiometricAuthManager {

    private const val PREFS_NAME = "coinbridge_security_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"

    fun isBiometricLockEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricLockEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun checkBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

        return when (val result = biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricStatus(
                    availability = BiometricAvailability.AVAILABLE,
                    description = "Biometrics & Device Credentials Ready",
                    canAuthenticate = true
                )
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricStatus(
                    availability = BiometricAvailability.NONE_ENROLLED,
                    description = "No biometric enrolled (Device PIN/Pattern available)",
                    canAuthenticate = true
                )
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                BiometricStatus(
                    availability = BiometricAvailability.NO_HARDWARE,
                    description = "No biometric hardware detected (Device PIN fallback)",
                    canAuthenticate = true
                )
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                BiometricStatus(
                    availability = BiometricAvailability.HW_UNAVAILABLE,
                    description = "Biometric hardware currently unavailable",
                    canAuthenticate = false
                )
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                BiometricStatus(
                    availability = BiometricAvailability.SECURITY_UPDATE_REQUIRED,
                    description = "Security update required for biometrics",
                    canAuthenticate = false
                )
            }
            else -> {
                BiometricStatus(
                    availability = BiometricAvailability.UNSUPPORTED,
                    description = "Status code: $result",
                    canAuthenticate = false
                )
            }
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Unlock CoinBridge",
        subtitle: String = "Verify your identity to access financial data",
        description: String? = "Use fingerprint, face, or device PIN to confirm access",
        negativeButtonText: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onCancel()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Biometric attempted but not recognized, user can retry
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)

        if (!description.isNullOrEmpty()) {
            promptInfoBuilder.setDescription(description)
        }

        // Support Device Credential (PIN / Pattern / Password) along with Biometrics
        // Note: setNegativeButtonText cannot be set if DEVICE_CREDENTIAL is included
        promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        val promptInfo = promptInfoBuilder.build()
        biometricPrompt.authenticate(promptInfo)
    }
}
