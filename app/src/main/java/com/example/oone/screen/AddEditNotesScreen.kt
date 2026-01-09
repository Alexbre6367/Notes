package com.example.oone.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oone.database.notes.Notes
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val localUser = "local_user"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NotesViewModel? = null,
    themeViewModel: ThemeViewModel,
    navController: NavHostController? = null,
    noteToEdit: Notes? = null,
) {
    var tempBody by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteToEdit?.body.orEmpty(),
                selection = TextRange(0) //позиционирование курсора noteToEdit?.body?.length ?: 0 - курсос будет в конце текста
            )
        )
    }
    var tempName by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteToEdit?.nameNote.orEmpty()
            )
        )
    }
    var tempStatus by remember { mutableStateOf(noteToEdit?.status ?: false) }
    var deleteActivated by remember { mutableStateOf(false) }
    var isDelete by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(noteToEdit?.state ?: false) }
    var lastEditTime by remember {
        mutableStateOf(noteToEdit?.lastEdited ?: LocalDateTime.now())
    }
    var tempAiStatus by remember { mutableStateOf(noteToEdit?.aiStatus ?: false) }
    val aiResult by viewModel?.analysisResult?.collectAsState() ?: remember { mutableStateOf(null) }

    fun LocalDateTime.formatBasedOnDate(): String {
        val today = LocalDate.now()
        val noteDate = this.toLocalDate()

        return if (noteDate == today) {
            this.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            this.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
        }
    }

    val colorRed = Color(209, 46, 36)
    val isDarkTheme by themeViewModel.isDarkTheme
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(noteToEdit) {
        if (noteToEdit != null) {
            focusRequester.freeFocus()
        } else {
            delay(100)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("MyLog", "Не удалось запросить фокус", e)
            }
        }
    }

    LaunchedEffect(deleteActivated) {
        if (deleteActivated) {
            delay(5000)
            deleteActivated = false
        }
    }

    fun saveNote() {
        val cleanBody = tempBody.text.trim()
        val cleanName = tempName.text.trim()

        if (cleanBody.isNotBlank()) {
            val currentUserId = Firebase.auth.currentUser?.uid ?: localUser

            viewModel?.addNote(
                Notes(
                    body = cleanBody,
                    status = tempStatus,
                    state = showPassword,
                    lastEdited = LocalDateTime.now(),
                    aiStatus = tempAiStatus,
                    nameNote = cleanName,
                    ownerId = currentUserId
                )
            )

        }
        keyboardController?.hide()
    }

    if (noteToEdit != null) {
        val isInitialComposition = remember { mutableStateOf(true) }
        LaunchedEffect(tempBody.text, tempName.text, tempStatus, showPassword) {
            if (isInitialComposition.value) {
                isInitialComposition.value = false
                return@LaunchedEffect
            }

            delay(500L)

            val cleanBody = tempBody.text.trim()
            val cleanName = tempName.text.trim()

            val originalBody = noteToEdit.body.trim()
            val originalName = noteToEdit.nameNote.trim()
            val hasChanged = cleanBody != originalBody || cleanName != originalName ||
                    tempStatus != noteToEdit.status || showPassword != noteToEdit.state

            if (hasChanged) {
                if (cleanBody.isBlank() && cleanName.isBlank()) {
                    viewModel?.deleteNote(noteToEdit.id)
                } else {
                    viewModel?.updateNote(
                        noteToEdit.copy(
                            body = cleanBody,
                            status = tempStatus,
                            state = showPassword,
                            lastEdited = LocalDateTime.now(),
                            aiStatus = tempAiStatus,
                            nameNote = cleanName
                        )
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (noteToEdit == null && !isDelete) {
                saveNote()
            }
        }
    }

    LaunchedEffect(aiResult) {
        aiResult?.let {
            if(tempAiStatus) {
                val lines = it.lines()
                val name = lines.firstOrNull()
                    ?.replace("#", "")?.replace("*", "")
                    ?.trim()
                    ?.replaceFirstChar { it ->
                        it.uppercaseChar()
                    } ?: ""
                val body = lines.drop(1)

                val cleanedBody = body.joinToString("\n") { line ->
                    line.replace("*", "").trimStart()
                }

                tempName = TextFieldValue(
                    text = name,
                )
                tempBody = TextFieldValue(
                    text = cleanedBody,
                )


                tempAiStatus = false
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                tempBody = TextFieldValue(
                    text = tempBody.text + it,
                    selection = TextRange(tempBody.text.length)
                )
            }
            viewModel?.clearAi() //очистка после анализа
        }
    }

    val coroutineScore = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { notNullUri ->
            viewModel?.recognizeTextFromImage(notNullUri, context.contentResolver)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val data = result.data //пакет данных
            val resultList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = resultList?.get(0) ?: ""

            if(recognizedText.isNotEmpty()) {
                val speechText = tempBody.text + recognizedText
                val prefix = if(tempBody.text.isNotBlank()) " " else ""

                tempBody = TextFieldValue(
                    text = tempBody.text + prefix + recognizedText)

                tempBody = TextFieldValue(
                    text = speechText,
                    selection = TextRange(speechText.length)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel?.errorLog?.collect { errorMassage ->
            Toast.makeText(
                context,
                errorMassage,
                Toast.LENGTH_LONG
            ).show()
            tempAiStatus = false
        }
    }

    BackHandler {
        focusManager.clearFocus()
        navController?.popBackStack()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColorBlack,
                    titleContentColor = backgroundColorWhite,
                    navigationIconContentColor = backgroundColorWhite
                ),
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        navController?.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },

                actions = {
                    IconButton(onClick = {
                        coroutineScore.launch {
                            if (tempBody.text.isNotBlank()) {
                                tempAiStatus = true
                                viewModel?.analyze(tempBody.text)
                            }
                        }
                    }) {
                        Icon(
                            imageVector =
                                if (tempAiStatus && tempBody.text.isNotBlank()) Icons.Default.AutoFixHigh
                                else Icons.Outlined.AutoFixHigh,
                            contentDescription = "AI",
                            tint = if (tempAiStatus && tempBody.text.isNotBlank()) colorRed else backgroundColorWhite
                        )
                    }

                    IconButton(onClick = {
                        tempStatus = !tempStatus
                    }) {
                        Icon(
                            imageVector = if (tempStatus) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Important",
                            tint = if (tempStatus) colorRed else backgroundColorWhite
                        )
                    }

                    IconButton(onClick = {
                        showPassword = !showPassword
                    }) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = if (showPassword) colorRed else backgroundColorWhite
                        )
                    }
                        IconButton(onClick = {
                            if (deleteActivated) {
                                if(noteToEdit != null) viewModel?.deleteNote(noteToEdit.id) else isDelete = true
                                navController?.popBackStack()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            } else {
                                deleteActivated = true
                            }
                        }) {
                            Icon(
                                imageVector = if (deleteActivated) Icons.Default.Check else Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = if (deleteActivated) colorRed else backgroundColorWhite
                            )
                        }
                },
            )
        },

        bottomBar = {
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding() // чтобы не залазило на панель навигации
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp + navBarHeight),
                containerColor = backgroundColorBlack,
                contentColor = backgroundColorWhite,
                tonalElevation = 0.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Log.d("MyLog", "${e.message}")
                            }
                        },
                        modifier = Modifier.align(alignment = Alignment.BottomStart)
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = backgroundColorWhite
                        )
                    }

                    Text(
                        modifier = Modifier.align(alignment = Alignment.Center),
                        text = "Edited ${lastEditTime.formatBasedOnDate()}",
                        color = backgroundColorWhite,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 17.sp
                    )

                    IconButton(
                        onClick = {
                            try {
                                launcher.launch("image/*")
                            } catch (e: Exception) {
                                Log.e("MyLog", "${e.message}")
                            }
                        },
                        modifier = Modifier
                            .align(alignment = Alignment.BottomEnd),
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            tint = backgroundColorWhite
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,

        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColorBlack)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (!tempAiStatus) {
                            try {
                                focusRequester.requestFocus()
                                tempBody = tempBody.copy(
                                    selection = TextRange(tempBody.text.length)
                                )
                            } catch (e: Exception) {
                                Log.e("MyLog", "${e.message}")
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColorBlack)
                        .padding(horizontal = 8.dp)
                        .padding(top = padding.calculateTopPadding(), bottom = 44.dp)
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState()),
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                        },
                        placeholder = {
                            Text(
                                "Name",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 24.sp
                            )
                        }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = backgroundColorWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp
                        )

                    )
                    if(tempAiStatus) {
                        AnimationGemini()
                    } else {
                        OutlinedTextField(
                            value = tempBody,
                            onValueChange = {
                                tempBody = it
                            },
                            placeholder = { Text("Text", style = MaterialTheme.typography.bodyLarge) }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = backgroundColorWhite,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}

fun Modifier.animatedGradient(
    colors: List<Color>
) : Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transient = rememberInfiniteTransition(label = "")

    val offsetXAnimation by transient.animateFloat(
        initialValue = -size.width.toFloat(),
        targetValue = size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    background(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(x = offsetXAnimation, y = 0f),
            end = Offset(x = offsetXAnimation + size.width.toFloat(), y = size.height.toFloat())
        ),
        shape = RoundedCornerShape(8.dp)
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun AnimationGemini() {
    val geminiColors = listOf(
        Color(0xFF4285F4),
        Color(0xFF9B72CB),
        Color(0xFFD96570),
        Color(0xFF4285F4)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .animatedGradient(geminiColors)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
                .animatedGradient(geminiColors)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(fraction = 0.7f)
                .animatedGradient(geminiColors)
        )
    }
}

