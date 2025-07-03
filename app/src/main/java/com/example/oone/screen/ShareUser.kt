package com.example.oone.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.ThemeViewModel

@Composable
fun ShareUser(
    themeViewModel: ThemeViewModel,
    navController: NavController,
    viewModel: NotesViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val colorRed = Color(209, 46, 36)
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    var email by remember { mutableStateOf(TextFieldValue()) }
    val isError by remember { mutableStateOf(false) }

    val animatedColor by animateColorAsState(
        targetValue = if (isError) colorRed else backgroundColorWhite,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }


    Box(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            .fillMaxSize()
            .background(backgroundColorBlack)
    ) {
        Column {
            Box(modifier = Modifier.padding(start = 8.dp, top = 50.dp)) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 55.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "Пригласить",
                    color = backgroundColorWhite,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(48.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = { Text(text = "Email пользователя")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = animatedColor,
                        focusedBorderColor = animatedColor,
                        unfocusedTextColor = animatedColor,
                        disabledTextColor = animatedColor,
                        errorTextColor = animatedColor
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColorWhite)
                ) {
                   Text("Добавить", color = backgroundColorBlack)
                }
            }
        }
    }
}