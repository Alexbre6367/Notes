package com.example.oone.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oone.database.notes.Notes
import com.example.oone.database.viewmodel.NotesViewModel
import com.example.oone.database.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NotesViewModel? = null,
    themeViewModel: ThemeViewModel,
    navController: NavHostController? = null,
    noteToEdit: Notes? = null,
){
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
    var tempStatus by remember { mutableStateOf(noteToEdit?.status ?: false ) }
    var deleteActivated by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(noteToEdit?.state ?: false) }
    var lastEditTime by remember {
        mutableStateOf(noteToEdit?.lastEdited ?: LocalDateTime.now())
    }
    var tempAiStatus by remember { mutableStateOf(noteToEdit?.aiStatus ?: false ) }
    val aiResult by viewModel?.analysisResult?.collectAsState() ?: remember { mutableStateOf(null) }

    fun LocalDateTime.formatBasedOnDate(): String {
        val today = LocalDate.now()
        val noteDate = this.toLocalDate()

        return if (noteDate == today) {
            this.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            this.format(DateTimeFormatter.ofPattern("d MMMM"))
        }
    }

    val colorRed = Color(209, 46, 36)
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(noteToEdit) {
        if(noteToEdit != null){
            focusRequester.freeFocus()
        } else {
            focusRequester.requestFocus()
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

        if(noteToEdit != null) {
            val originalBody = noteToEdit.body.trim()
            val originalName = noteToEdit.nameNote.trim()
            val originalStatus = noteToEdit.status
            val originalState = noteToEdit.state

            val hasChanged = cleanBody != originalBody || tempStatus != originalStatus || showPassword != originalState || cleanName != originalName

            if (cleanBody.isBlank()) {
                viewModel?.deleteNote(noteToEdit.id)
            } else if (hasChanged) {
                viewModel?.updateNote(noteToEdit.copy(
                    body = cleanBody,
                    status = tempStatus,
                    state = showPassword,
                    lastEdited = LocalDateTime.now(),
                    aiStatus = tempAiStatus,
                    nameNote = cleanName
                ))
            }
        } else {
            if(cleanBody.isNotBlank()) {
                viewModel?.addNote(Notes(
                    body = cleanBody,
                    status = tempStatus,
                    state = showPassword,
                    lastEdited = LocalDateTime.now(),
                    aiStatus = tempAiStatus,
                    nameNote = cleanName
                ))
            }
        }
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    LaunchedEffect(aiResult) {
        aiResult?.let {
            val lines = it.lines()
            val name = lines.firstOrNull()
                ?.replace("#", "")?.replace("*", "")
                ?.trim()
                ?.replaceFirstChar {
                    it -> it.uppercaseChar()
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

            viewModel?.clearAi() //очистка после анализа

            saveNote()
        }
    }

    val coroutineScore = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel?.errorLog?.collect { errorMassage ->
            Toast.makeText(
                context,
                errorMassage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    BackHandler {
        if(!tempAiStatus) {
            saveNote()
        }
        navController?.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(90.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColorBlack,
                    titleContentColor = backgroundColorWhite,
                    navigationIconContentColor = backgroundColorWhite
                ),
                title = {  },
                navigationIcon = {
                    IconButton(onClick = {
                        if(!tempAiStatus) {
                            saveNote()
                        }
                        navController?.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },

                actions = {
                        IconButton(onClick = {
                            coroutineScore.launch {
                                if(tempBody.text.isNotBlank()) {
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
                            imageVector = if (tempStatus) Icons.Default.Favorite  else Icons.Default.FavoriteBorder,
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
                            if(noteToEdit == null) {
                                navController?.popBackStack()
                                keyboardController?.hide()
                            } else if (deleteActivated) {
                                viewModel?.deleteNote(noteToEdit.id)
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
            val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() // чтобы не залазило на панель навигации
            BottomAppBar(
                modifier = Modifier.fillMaxWidth().height(40.dp + navBarHeight),
                containerColor = backgroundColorBlack,
                contentColor = backgroundColorWhite,
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                        Text(
                            text = "Изменено ${lastEditTime.formatBasedOnDate()}",
                            color = backgroundColorWhite,
                            style = MaterialTheme.typography.bodyMedium,
                            )

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
                        focusRequester.requestFocus()
                        tempBody = tempBody.copy(
                            selection = TextRange(tempBody.text.length)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColorBlack)
                        .padding(horizontal = 8.dp)
                        .padding(top = padding.calculateTopPadding())
                        .navigationBarsPadding()
                        .padding(bottom = if (!isKeyboardOpen) 44.dp else 0.dp)
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                ) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it
                        },
                        placeholder = { Text("Название", fontSize = 24.sp) }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
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
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    OutlinedTextField(
                        value = tempBody,
                        onValueChange = {
                            tempBody = it
                        },
                        placeholder = { Text("Текст") }, //label text уходит на рамку, placeholder text пропадает при взаимодействии
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = backgroundColorWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    )
}
