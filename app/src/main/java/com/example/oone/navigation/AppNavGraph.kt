package com.example.oone.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.PlaceViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import com.example.oone.screen.AddEditNoteScreen
import com.example.oone.screen.NotesScreen
import com.example.oone.screen.SettingScreen
import com.example.oone.screen.login.AccountScreen
import com.example.oone.screen.login.LoginScreen


@Composable
fun AppNavGraph(
    viewModel: NotesViewModel,
    themeViewModel: ThemeViewModel,
    navController: NavHostController,
    placeViewModel: PlaceViewModel,
    activity: FragmentActivity,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = "note_screen",
        ) {
            composable(route = "note_screen") {
                NotesScreen(
                    viewModel = viewModel,
                    themeViewModel = themeViewModel,
                    navController = navController,
                    placeViewModel = placeViewModel,
                    activity = activity
                )
            }
            composable(
                route = "add_note",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) {
                AddEditNoteScreen(
                    viewModel = viewModel,
                    navController = navController,
                    themeViewModel = themeViewModel,
                )
            }
            composable(
                route = "edit_note/{noteId}", // стандартный путь + Id выбранной заметки
                arguments = listOf(navArgument("noteId") {
                    type = NavType.IntType // марштрут определяет Id заметки
                }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId") // извлечение переданного Id
                val note = viewModel.notesList.value?.find { it.id == noteId } // поиск заметки с нужным Id в BD
                if (note != null) {
                    AddEditNoteScreen(
                        navController = navController,
                        viewModel = viewModel,
                        themeViewModel = themeViewModel,
                        noteToEdit = note, // передача заметки
                    )
                }
            }

            composable(
                route = "login_screen",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) {
                LoginScreen(
                    themeViewModel = themeViewModel,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable(
                route = "account_screen",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) {
                AccountScreen(
                    themeViewModel = themeViewModel,
                    navController = navController,
                    viewModel = viewModel,
                    activity = activity
                )
            }
            composable(
                route = "setting_screen",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            ) {
                SettingScreen(
                    viewModel = viewModel,
                    themeViewModel = themeViewModel,
                    navController = navController,
                    placeViewModel = placeViewModel
                )
            }
        }
    }
}

