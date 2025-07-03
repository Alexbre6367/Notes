package com.example.oone.screen


 import androidx.activity.compose.BackHandler
 import androidx.compose.foundation.background
 import androidx.compose.foundation.clickable
 import androidx.compose.foundation.interaction.MutableInteractionSource
 import androidx.compose.foundation.layout.Arrangement
 import androidx.compose.foundation.layout.Box
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.Row
 import androidx.compose.foundation.layout.Spacer
 import androidx.compose.foundation.layout.WindowInsets
 import androidx.compose.foundation.layout.asPaddingValues
 import androidx.compose.foundation.layout.fillMaxHeight
 import androidx.compose.foundation.layout.fillMaxSize
 import androidx.compose.foundation.layout.fillMaxWidth
 import androidx.compose.foundation.layout.height
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.statusBars
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.items
 import androidx.compose.foundation.rememberScrollState
 import androidx.compose.foundation.verticalScroll
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.filled.Send
 import androidx.compose.material.icons.filled.AccountCircle
 import androidx.compose.material.icons.filled.Add
 import androidx.compose.material.icons.filled.AutoAwesomeMotion
 import androidx.compose.material.icons.filled.Check
 import androidx.compose.material.icons.filled.Delete
 import androidx.compose.material.icons.filled.Edit
 import androidx.compose.material.icons.filled.Menu
 import androidx.compose.material.icons.filled.Settings
 import androidx.compose.material3.Button
 import androidx.compose.material3.ButtonColors
 import androidx.compose.material3.ButtonDefaults
 import androidx.compose.material3.DrawerValue
 import androidx.compose.material3.ExperimentalMaterial3Api
 import androidx.compose.material3.FloatingActionButton
 import androidx.compose.material3.FloatingActionButtonDefaults
 import androidx.compose.material3.Icon
 import androidx.compose.material3.IconButton
 import androidx.compose.material3.MaterialTheme
 import androidx.compose.material3.ModalDrawerSheet
 import androidx.compose.material3.ModalNavigationDrawer
 import androidx.compose.material3.NavigationDrawerItem
 import androidx.compose.material3.NavigationDrawerItemDefaults
 import androidx.compose.material3.Scaffold
 import androidx.compose.material3.Text
 import androidx.compose.material3.TopAppBar
 import androidx.compose.material3.TopAppBarDefaults
 import androidx.compose.material3.rememberDrawerState
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.LaunchedEffect
 import androidx.compose.runtime.collectAsState
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.livedata.observeAsState
 import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.remember
 import androidx.compose.runtime.rememberCoroutineScope
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import androidx.fragment.app.FragmentActivity
 import androidx.navigation.NavController
 import com.example.oone.database.notes.Notes
 import com.example.oone.database.viewmodel.NotesViewModel
 import com.example.oone.database.viewmodel.PlaceViewModel
 import com.example.oone.database.viewmodel.ThemeViewModel
 import com.google.firebase.auth.FirebaseAuth
 import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    themeViewModel: ThemeViewModel,
    navController: NavController,
    placeViewModel: PlaceViewModel,
    activity: FragmentActivity,
) {

    val userNotes by viewModel.notesList.observeAsState(emptyList())

    val sortedNotes = remember(userNotes) {
        userNotes.sortedWith(
            compareByDescending<Notes> { it.status }
                .thenByDescending { it.aiStatus }
                .thenByDescending { it.id }
        )
    }

    val selectedId by viewModel.selectedNoteId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotesFromFirestore()//из Firestore
    }
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    var deleteActivated by remember { mutableStateOf(false) }

    val isPlaceActivated by placeViewModel.isPlaceActivated.collectAsState()
    val alignmentEnd = if(isPlaceActivated) Alignment.BottomStart else Alignment.BottomEnd

    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val backgroundColorBlack = if (isDarkTheme) Color.Black else Color.White
    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black
    val colorRed = Color(209, 46, 36)

    BackHandler(enabled = selectedNoteId.isNotEmpty()) {
        viewModel.clearSelection()
        deleteActivated = false
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val score = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContentColor = backgroundColorWhite,
                drawerContainerColor = backgroundColorBlack
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Notes",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    NavigationDrawerItem(
                        label = { Text("Заметки") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            score.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = backgroundColorWhite,
                            unselectedTextColor = backgroundColorBlack,
                            unselectedIconColor = backgroundColorBlack
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text("Настройки") },
                        selected = false,
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        badge = {  },
                        onClick = {
                            navController.navigate("setting_screen")
                            score.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = backgroundColorWhite,
                            unselectedIconColor = backgroundColorWhite
                        )

                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColorBlack,
                        titleContentColor = backgroundColorWhite,
                        navigationIconContentColor = backgroundColorWhite
                    ),
                    navigationIcon = {
                        if(selectedId.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.toggleNoteAll()
                                },
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesomeMotion,
                                    contentDescription = "All",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    score.launch {
                                        drawerState.open()
                                    }
                                    if (viewModel.selectedNoteId.value.isNotEmpty()) {
                                        viewModel.clearSelection()
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Notes",
                                fontSize = 24.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (selectedNoteId.isNotEmpty()) {
                                    if (deleteActivated) {
                                        viewModel.deleteSelectedNotes()
                                        deleteActivated = false
                                    } else {
                                        deleteActivated = true
                                    }
                                } else {
                                    if (user != null) {
                                        navController.navigate("account_screen")
                                    } else {
                                        navController.navigate("login_screen")
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                imageVector = when {
                                    selectedNoteId.isNotEmpty() && deleteActivated -> Icons.Default.Check
                                    selectedNoteId.isNotEmpty() -> Icons.Default.Delete
                                    else -> Icons.Default.AccountCircle
                                },
                                contentDescription = "Account or Delete",
                                tint = if(deleteActivated) Color.Red else backgroundColorWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            },
            content = { padding->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColorBlack)
                        .padding(top = 60.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (viewModel.selectedNoteId.value.isNotEmpty()) {
                                viewModel.clearSelection()
                            }
                        }
                ) {
                    if (userNotes.isEmpty()) {
                        EmptyState(isDarkTheme = isDarkTheme)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                                    .padding(
                                        top = WindowInsets.statusBars.asPaddingValues()
                                            .calculateTopPadding()
                                    )
                            ) {
                                val pinnedNotes = sortedNotes.filter { it.status }
                                val aiNotes = sortedNotes.filter { it.aiStatus && !it.status}
                                val otherNotes = sortedNotes.filter { !it.status && !it.aiStatus}

                                val pinnedNotesSorted = pinnedNotes.sortedByDescending { it.id }
                                val aiNotesSorted = aiNotes.sortedByDescending { it.id }
                                val otherNotesSorted = otherNotes.sortedByDescending { it.id }

                                if (pinnedNotesSorted.isNotEmpty() || aiNotesSorted.isNotEmpty()) {
                                    if (pinnedNotesSorted.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Закрепленные",
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    top = 8.dp,
                                                    bottom = 12.dp
                                                ),
                                                color = backgroundColorWhite,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                        items(pinnedNotesSorted) { note ->
                                            NoteItem(
                                                note = note,
                                                onEdit = { navController.navigate("edit_note/${note.id}") },
                                                isDarkTheme = isDarkTheme,
                                                viewModel = viewModel,
                                                activity = activity,
                                                isSelected = selectedId.isNotEmpty()
                                            )
                                        }
                                    }

                                    if (aiNotesSorted.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "С AI",
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    top = 8.dp,
                                                    bottom = 12.dp
                                                ),
                                                color = backgroundColorWhite,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                        items(aiNotesSorted) { note ->
                                            NoteItem(
                                                note = note,
                                                onEdit = { navController.navigate("edit_note/${note.id}") },
                                                isDarkTheme = isDarkTheme,
                                                viewModel = viewModel,
                                                activity = activity,
                                                isSelected = selectedId.isNotEmpty()
                                            )
                                        }
                                    }

                                    if (otherNotesSorted.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "Другие",
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    top = 16.dp,
                                                    bottom = 12.dp
                                                ),
                                                color = backgroundColorWhite,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                        items(otherNotesSorted) { note ->
                                            NoteItem(
                                                note = note,
                                                onEdit = { navController.navigate("edit_note/${note.id}") },
                                                isDarkTheme = isDarkTheme,
                                                viewModel = viewModel,
                                                activity = activity,
                                                isSelected = selectedId.isNotEmpty()
                                            )
                                        }
                                    }
                                } else {
                                    items(sortedNotes) { note ->
                                        NoteItem(
                                            note = note,
                                            onEdit = { navController.navigate("edit_note/${note.id}") },
                                            isDarkTheme = isDarkTheme,
                                            viewModel = viewModel,
                                            activity = activity,
                                            isSelected = selectedId.isNotEmpty()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (selectedNoteId.isNotEmpty()) {
                                if (deleteActivated) {
                                    viewModel.deleteSelectedNotes()
                                    deleteActivated = false
                                } else {
                                    deleteActivated = true
                                }
                            } else {
                                navController.navigate("add_note")
                            }
                        },
                        modifier = Modifier
                            .align(alignmentEnd)
                            .padding(vertical = 40.dp, horizontal = 20.dp)
                            .size(60.dp),
                        containerColor = colorRed,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyState(isDarkTheme: Boolean) {

    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "No notes",
            tint = backgroundColorWhite,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "Здесь будут ваши заметки",
            color = backgroundColorWhite,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}



