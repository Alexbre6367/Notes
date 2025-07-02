package com.example.oone.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.PlaceViewModel
import com.example.oone.database.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: NotesViewModel,
    themeViewModel: ThemeViewModel,
    navController: NavController,
    placeViewModel: PlaceViewModel,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPlaceActivated by placeViewModel.isPlaceActivated.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(start = 8.dp, top = 50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("note_screen") }
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "back",
                        tint = backgroundColorWhite
                    )
                }
                Text(
                    text = "Настройки",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 20.sp,
                    color = backgroundColorWhite,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 15.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Параметры отображения",
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 25.sp,
                    color = backgroundColorWhite,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(28.dp))
                Row(modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = interactionSource
                    ) {
                        themeViewModel.toggleTheme()
                    },
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Светлая тема",
                        color = backgroundColorWhite,
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp
                    )
                    Switch(
                        checked =! isDarkTheme,
                        onCheckedChange = { themeViewModel.toggleTheme() },
                        Modifier.scale(1.2f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.Transparent,
                            checkedBorderColor = Color.Black,

                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = Color.White,
                        )
                    )
                }

                Spacer(Modifier.height(28.dp))
                Row(modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource
                        ) {
                            placeViewModel.togglePlacePosition()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Для левшей",
                        color = backgroundColorWhite,
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp
                    )
                    Switch(
                        checked = isPlaceActivated,
                        onCheckedChange = { placeViewModel.togglePlacePosition() },
                        Modifier.scale(1.2f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = backgroundColorWhite,
                            checkedTrackColor = Color.Transparent,
                            checkedBorderColor = backgroundColorWhite,

                            uncheckedThumbColor = backgroundColorWhite,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = backgroundColorWhite,
                        )
                    )
                }
            }
        }
    }
}