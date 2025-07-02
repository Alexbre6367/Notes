@file:Suppress("DEPRECATION")

package com.example.oone.screen

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SetStatusBar(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            val window = activity.window

            WindowCompat.setDecorFitsSystemWindows(window, false)

            window.run {
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

                isNavigationBarContrastEnforced = false

                statusBarColor = Color.Transparent.toArgb()
                navigationBarColor = Color.Transparent.toArgb()
            }

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }
}