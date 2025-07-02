package com.example.oone.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity


fun authenticate(context: FragmentActivity, onAuthenticationSucceeded: () -> Unit) {

    val executor = context.mainExecutor
    val biometricPrompt = BiometricPrompt(
        context,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                onAuthenticationSucceeded()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Разблокируйте, чтобы продолжить")
        .setNegativeButtonText("Отмена")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
    biometricPrompt.authenticate(promptInfo)
}