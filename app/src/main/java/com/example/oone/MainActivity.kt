package com.example.oone

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.NotesViewModelFactory
import com.example.oone.database.viewmodel.PlaceViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import com.example.oone.database.viewmodel.ThemeViewModelFactory
import com.example.oone.navigation.AppNavGraph
import com.example.oone.screen.SetStatusBar
import com.example.oone.ui.theme.OOneTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemTheme = isSystemInDarkTheme()

            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(initialDarkTheme = systemTheme)
            )
            val placeViewModel = viewModel<PlaceViewModel>()
            val isDarkTheme by themeViewModel.isDarkTheme
            val navController = rememberNavController()

            SetStatusBar(isDarkTheme)

            OOneTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current
                val viewModel: NotesViewModel = viewModel(
                    factory = NotesViewModelFactory(context.applicationContext as Application)
                )

                val activity = context as FragmentActivity

                AppNavGraph(
                    viewModel = viewModel,
                    themeViewModel = themeViewModel,
                    navController = navController,
                    placeViewModel = placeViewModel,
                    activity = activity
                )
            }
        }
    }
}
