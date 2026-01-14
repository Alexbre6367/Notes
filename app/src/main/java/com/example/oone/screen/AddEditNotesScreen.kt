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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.oone.ui.theme.borderColor
import com.example.oone.ui.theme.geminiColors
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val localUser = "local_user"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var promptText by remember { mutableStateOf("") }
    var tempStatus by remember { mutableStateOf(noteToEdit?.status ?: false) }
    var deleteActivated by remember { mutableStateOf(false) }
    var isDelete by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(noteToEdit?.state ?: false) }
    var lastEditTime by remember {
        mutableStateOf(noteToEdit?.lastEdited ?: LocalDateTime.now())
    }
    var tempAiStatus by remember { mutableStateOf(noteToEdit?.aiStatus ?: false) }
    val aiResult by viewModel?.analysisResult?.collectAsState() ?: remember { mutableStateOf(null) }
    val undoStack = remember { mutableListOf<TextFieldValue>() }
    val redoStack = remember { mutableListOf<TextFieldValue>() }
    var askGemini by remember { mutableStateOf(false) }

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
    val geminiFocusRequester = remember { FocusRequester() }
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

    LaunchedEffect(askGemini) {
        if (askGemini) {
            delay(100)
            try {
                geminiFocusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("MyLog", "Не удалось запросить фокус", e)
            }
        }
    }


    var more by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    LaunchedEffect(deleteActivated) {
        if (deleteActivated) {
            delay(5000)
            deleteActivated = false
        }
    }

    fun saveNote() {
        val cleanBody = tempBody.text.trim()
        val cleanName = tempName.text.trim()

        if (cleanBody.isNotBlank() || cleanName.isNotBlank()) {
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
            if (tempAiStatus) {
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExiting by remember { mutableStateOf(false) }

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
        if (result.resultCode == Activity.RESULT_OK) {
            val recognizedText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (recognizedText.isNotEmpty()) {
                val prefix = if (tempBody.text.isNotBlank()) " " else ""
                val newText = tempBody.text + prefix + recognizedText
                tempBody = TextFieldValue(text = newText, selection = TextRange(newText.length))
            }
        }
    }

    val geminiSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognizedText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (recognizedText.isNotEmpty()) {
                promptText = recognizedText
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

    LaunchedEffect(isExiting) {
        if (isExiting) {
            delay(350)
            isExiting = false
        }
    }

    BackHandler {
        focusManager.clearFocus()
        if (askGemini) {
            askGemini = false
        } else {
            isExiting = true
            navController?.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColorBlack)
            .statusBarsPadding()
            .padding(top = 6.dp)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                        },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Name",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 30.sp
                            )
                        }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = backgroundColorWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 32.sp
                        )
                    )

                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(color = borderColor, shape = CircleShape)
                            .animateContentSize(
                                animationSpec = tween(durationMillis = 250)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if(menu) {
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
                            IconButton(
                                onClick = {
                                    if (deleteActivated) {
                                        isExiting = true
                                        if(noteToEdit != null) viewModel?.deleteNote(noteToEdit.id) else isDelete = true
                                        navController?.popBackStack()
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    } else {
                                        deleteActivated = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (deleteActivated) Icons.Default.Check else Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = if (deleteActivated) colorRed else backgroundColorWhite
                                )
                            }
                        }
                        IconButton(
                            onClick = { menu = !menu },
                            modifier = Modifier.background(borderColor, shape = CircleShape)
                        ) {
                            Icon(
                                Icons.Outlined.Tune,
                                contentDescription = "Menu",
                                tint = backgroundColorWhite
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = borderColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    if (tempAiStatus) {
                        AnimationGemini()
                    } else {
                        OutlinedTextField(
                            value = tempBody,
                            onValueChange = { newValue ->
                                if (newValue.text != tempBody.text) {
                                    if (undoStack.size > 50) undoStack.removeAt(0)
                                    undoStack.add(tempBody)
                                    redoStack.clear()
                                }
                                tempBody = newValue
                            },
                            placeholder = {
                                Text(
                                    "Text",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = com.example.oone.ui.theme.colorRed
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColorBlack)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edited ${lastEditTime.formatBasedOnDate()}",
                        color = backgroundColorWhite,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Words: ${
                            tempBody.text.split(" ").filter { it.isNotBlank() }.size
                        }",
                        color = backgroundColorWhite,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = borderColor,
                                shape = CircleShape
                            )
                    ) {
                        IconButton(
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    val lastValue =
                                        undoStack.removeAt(undoStack.size - 1)
                                    redoStack.add(tempBody)
                                    tempBody = lastValue
                                }
                            },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Undo",
                                tint = backgroundColorWhite,
                            )
                        }

                        IconButton(
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    val nextValue =
                                        redoStack.removeAt(redoStack.size - 1)
                                    undoStack.add(tempBody)
                                    tempBody = nextValue
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = "Redo",
                                tint = backgroundColorWhite,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(color = borderColor, shape = CircleShape)
                            .animateContentSize(
                                animationSpec = tween(durationMillis = 250)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (more) {
                            IconButton(
                                onClick = {
                                    askGemini = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = backgroundColorWhite
                                )
                            }

                            IconButton(
                                onClick = {
                                    val intent =
                                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                    try {
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Log.d("MyLog", "${e.message}")
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.KeyboardVoice,
                                    contentDescription = "Voice",
                                    tint = backgroundColorWhite,
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        launcher.launch("image/*")
                                    } catch (e: Exception) {
                                        Log.e("MyLog", "${e.message}")
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = backgroundColorWhite,
                                )
                            }

                            IconButton(
                                onClick = {

                                },
                            ) {
                                Icon(
                                    Icons.Outlined.Timer,
                                    contentDescription = "Timer",
                                    tint = backgroundColorWhite,
                                )
                            }
                        }

                        IconButton(
                            onClick = { more = !more }
                        ) {
                            Icon(
                                if (more) Icons.AutoMirrored.Default.ArrowForward else Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = backgroundColorWhite,
                            )
                        }
                    }
                }
            }
        }

        if(isExiting) {
            Box(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true)
                            awaitPointerEvent()
                    }
                }
            )
        }

        if (askGemini) {
            val animatedBrush = rememberAnimationBorder(colors = geminiColors)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            askGemini = false
                        }
                    }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .navigationBarsPadding()
                    .background(
                        color = borderColor,
                        shape = RoundedCornerShape(34.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = animatedBrush,
                        shape = RoundedCornerShape(34.dp)
                    )
                    .padding(horizontal = 8.dp)
                    .heightIn(min = 80.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = {
                        promptText = it
                    },
                    placeholder = {
                        Text(
                            "Ask Gemini",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(geminiFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = com.example.oone.ui.theme.colorRed
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        try {
                            geminiSpeechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Log.d("MyLog", "${e.message}")
                        }
                    }
                ) {
                    Icon(
                        Icons.Outlined.KeyboardVoice,
                        contentDescription = "Voice",
                        tint = backgroundColorWhite,
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (tempBody.text.isNotBlank() || promptText.isNotBlank()) {
                                tempAiStatus = true
                                val fullQuery = """
                                   ${tempBody.text}
                                    
                                    $promptText
                                """.trimIndent()

                                viewModel?.analyze(fullQuery)
                                promptText = ""
                                askGemini = false
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


fun Modifier.animatedGradient(
    colors: List<Color>
): Modifier = composed {
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

@Composable
fun rememberAnimationBorder(
    colors: List<Color>,
    durationMillis: Int = 1500,
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(offset, offset),
        end = Offset(offset + 500f, offset + 500f),
        tileMode = TileMode.Mirror

    )
}
