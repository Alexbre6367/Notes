package com.example.oone.screen.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    themeViewModel: ThemeViewModel,
    navController: NavController,
    viewModel: NotesViewModel
) {
    val auth = FirebaseAuth.getInstance()

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    Log.d("MyLog", "User email: ${auth.currentUser?.email}")//проверка

    val colorRed = Color(209, 46, 36)
    val isDarkTheme by themeViewModel.isDarkTheme
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var isErrorEmail by remember { mutableStateOf(false) }
    var isErrorPassword by remember { mutableStateOf(false) }

    val animatedColorEmail by animateColorAsState(
        targetValue = if (isErrorEmail) colorRed else Color.Gray,
        animationSpec = tween(durationMillis = 500)
    )
    val animatedColorPassword by animateColorAsState(
        targetValue = if (isErrorPassword) colorRed else Color.Gray,
        animationSpec = tween(durationMillis = 500)
    )

    var openDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if(isErrorEmail) {
        LaunchedEffect(Unit) {
            delay(5000)
            isErrorEmail = false
        }
    }

    if(isErrorPassword) {
        LaunchedEffect(Unit) {
            delay(5000)
            isErrorPassword = false
        }
    }


    BackHandler {
        navController.navigate("note_screen")
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColorBlack)
        ) {
            Box(modifier = Modifier.padding(start = 8.dp, top = 50.dp)) {
                IconButton(
                    onClick = {
                        navController.navigate("note_screen")
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "Войти",
                    color = backgroundColorWhite,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(48.dp))
                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = {
                        emailState.value = it
                    },
                    placeholder = { Text("Адрес эл. почты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = animatedColorEmail,
                        focusedBorderColor = animatedColorEmail,
                        unfocusedBorderColor = animatedColorEmail,
                        disabledBorderColor = animatedColorEmail,
                        errorBorderColor = animatedColorEmail
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = {
                        passwordState.value = it
                    },
                    placeholder = { Text("Пароль(от 6 символов)") },
                    singleLine = true,
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "info",
                                tint = if (passwordVisibility) colorRed else Color.Gray
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = animatedColorPassword,
                        focusedBorderColor = animatedColorPassword,
                        unfocusedBorderColor = animatedColorPassword,
                        disabledBorderColor = animatedColorPassword,
                        errorBorderColor = animatedColorPassword
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (emailState.value.isNotBlank() && passwordState.value.isNotBlank()) {
                            signIn(auth, emailState.value, passwordState.value) { isSuccess ->
                                if (isSuccess) {
                                    viewModel.setUserCredentials(
                                        emailState.value,
                                        passwordState.value
                                    )
                                    navController.navigate("account_screen")
                                } else {
                                    isErrorEmail = true
                                    isErrorPassword = true
                                }
                            }
                        } else {
                            isErrorEmail = true
                            isErrorPassword = true
                            Log.d("MyLog", "Пустое поле")
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColorWhite)

                ) {
                    Text("Продолжить", color = backgroundColorBlack)
                }

                Spacer(modifier = Modifier.height(24.dp))
                val email = emailState.value.trim()
                TextButton(
                    onClick = {
                        if(email.isNotBlank()) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Письмо для смены пароля отправлено на $email",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Ошибка отправки письма",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.d("MyLog", "Ошибка сброса пароля")
                                    }
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "Введите почту",
                                Toast.LENGTH_LONG
                            ).show()
                            isErrorEmail = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text(
                        text = "Забыли пароль?",
                        color = backgroundColorWhite
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp),
                        thickness = DividerDefaults.Thickness, color = Color.Gray
                    )
                    Text(
                        text = "или",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = Color.Gray
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp),
                        thickness = DividerDefaults.Thickness, color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = {
                        openDialog = !openDialog
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColorWhite)
                ) {
                    Text(
                        text = "Продолжить c Google",
                        color = backgroundColorBlack,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (emailState.value.isNotBlank() && passwordState.value.length >= 4) {
                            signUp(auth, emailState.value, passwordState.value) { isSuccess ->
                                if (isSuccess) {
                                    viewModel.setUserCredentials(
                                        emailState.value,
                                        passwordState.value
                                    )
                                    navController.navigate("account_screen")
                                } else {
                                    isErrorEmail = true
                                    isErrorPassword = true
                                }
                            }
                        } else {
                            isErrorEmail = true
                            isErrorPassword = true
                            Log.d("MyLog", "Пустое поле")
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColorWhite)
                ) {
                    Text(
                        text = "Создать учетную запись",
                        color = backgroundColorBlack
                    )
                }
                if (openDialog) {
                    AlertDialog(
                        onDismissRequest = { openDialog = false },
                        title = { Text(text = "В разработке") },
                        confirmButton = {
                            Button(
                                { openDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColorWhite)
                            ) {
                                Text("OK")
                            }
                        },
                        containerColor = Color.DarkGray,
                        titleContentColor = backgroundColorWhite,
                    )
                }
            }
        }
    }
}



private fun signUp(auth: FirebaseAuth, email: String, password: String, onResult: (Boolean) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            onResult(it.isSuccessful)
            if(it.isSuccessful) {
                Log.d("MyLog", "Успешная регистрация")
            } else {
                Log.d("MyLog", "Регистрация не пройдена")
            }
        }
}

private fun signIn(auth: FirebaseAuth, email: String, password: String, onResult: (Boolean) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            onResult(it.isSuccessful)
            if(it.isSuccessful) {
                Log.d("MyLog", "Успешный вход")
            } else {
                Log.d("MyLog", "Ошибка входа")
            }
        }
}

