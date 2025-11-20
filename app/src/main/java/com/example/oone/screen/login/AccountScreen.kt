package com.example.oone.screen.login

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.oone.auth.authenticate
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AccountScreen(
    themeViewModel: ThemeViewModel,
    navController: NavController,
    viewModel: NotesViewModel,
    activity: FragmentActivity,
) {
    val auth = FirebaseAuth.getInstance()

    Log.d("MyLog", "User email: ${auth.currentUser?.email}")

    val currentUser = FirebaseAuth.getInstance().currentUser
    val emailUser = currentUser?.email ?: "Не авторизован"


    val colorRed = Color(209, 46, 36)
    val isDarkTheme by themeViewModel.isDarkTheme
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    var isDeleteAccount by remember { mutableStateOf(false) }

    val currentEmail = viewModel.userEmail.value
    val currentPassword = viewModel.userPassword.value
    val context = LocalContext.current

    BackHandler {
        navController.navigate("note_screen")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorBlack)
    ) {
        Box(modifier = Modifier.padding(start = 8.dp, top = 50.dp)) {
            IconButton(
                onClick = {
                    navController.navigate("note_screen")
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(65.dp))
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "account",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                tint = backgroundColorWhite
            )
            Spacer(modifier = Modifier.padding(12.dp))
            Text(
                text = emailUser,
                color = backgroundColorWhite,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailUser)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Письмо для смены пароля отправлено на $emailUser",
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
                    text = "Смена пароля",
                    color = backgroundColorWhite
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = {
                    authenticate(activity) {
                        signOut(auth)
                        navController.navigate("login_screen")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColorWhite)
            ) {
                Text("Выйти")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if(isDeleteAccount) {
                        authenticate(activity) {
                            if (currentEmail.isNotEmpty() && currentPassword.isNotEmpty()) {
                                viewModel.deleteUserAndNotes(auth, currentEmail, currentPassword) { success ->
                                    deleteAccount(auth, currentEmail, currentPassword)
                                    navController.navigate("login_screen")
                                }

                            } else {
                                Log.d("MyLog", "Нет данных для удаления")
                            }
                        }
                    } else {
                        isDeleteAccount = true
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColorWhite)
            ) {
                Text(
                    text = if(isDeleteAccount) "Все данные будут удалены" else "Удалить аккаунт",
                    color = if (isDeleteAccount) colorRed else backgroundColorBlack
                )
            }
        }
    }
}

private fun signOut(auth: FirebaseAuth) {
    auth.signOut()
}

private fun deleteAccount(auth: FirebaseAuth, email: String, password: String) {
    val credential = EmailAuthProvider.getCredential(email, password)
    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { it ->
        if(it.isSuccessful) {
            auth.currentUser?.delete()?.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d("MyLog", "Аккаунт удалён")
                } else {
                    Log.d("MyLog", "Ошибка удаления")
                }
            }
        } else {
            Log.d("MyLog", "Ошибка повторной аутентификаци")
        }
    }
}

