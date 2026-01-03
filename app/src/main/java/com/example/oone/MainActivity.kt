package com.example.oone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.NotesViewModelFactory
import com.example.oone.database.viewmodel.ThemeViewModel
import com.example.oone.database.viewmodel.ThemeViewModelFactory
import com.example.oone.navigation.AppNavGraph
import com.example.oone.ui.theme.OOneTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTransparentSystemBars()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(
                    application = application,
                    initialDarkTheme = systemTheme
                )
            )
            val isDarkTheme by themeViewModel.isDarkTheme

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !isDarkTheme
                        isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }
            }

            OOneTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val viewModel: NotesViewModel = viewModel(
                    factory = NotesViewModelFactory(context.applicationContext as Application)
                )
                val activity = context as FragmentActivity

                AppNavGraph(
                    viewModel = viewModel,
                    themeViewModel = themeViewModel,
                    navController = navController,
                    activity = activity
                )
            }

        }
    }

    private fun setupTransparentSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}
