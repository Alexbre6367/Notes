package com.example.oone.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.oone.auth.authenticate
import com.example.oone.database.notes.Notes
import com.example.oone.database.viewmodel.NotesViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Notes,
    onEdit: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: NotesViewModel,
    activity: FragmentActivity,
    isSelected: Boolean
){
    val borderColor = Color(52, 52, 52)

    val backgroundColorWhite = if (isDarkTheme) Color.White else Color.Black
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()

    if (note.id in selectedNoteId) {
        LaunchedEffect(note.id) {
            delay(10000)
            viewModel.toggleNoteSelection(note.id)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 65.dp, max = 250.dp)
            .padding(vertical = 4.dp)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    if(isSelected) {
                        viewModel.toggleNoteSelection(note.id)
                    } else {
                        if (note.state) {
                            authenticate(activity) {
                                onEdit()
                            }
                        } else {
                            onEdit()
                        }
                    }
                },
                onLongClick = {
                    viewModel.toggleNoteSelection(note.id)
                }
            )
            .border(
                width = if(note.id in selectedNoteId) 2.dp else 1.dp,
                color = if(note.id in selectedNoteId) backgroundColorWhite else borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(note.state) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = backgroundColorWhite,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(modifier = Modifier.weight(1f)){
                if (!note.state && note.nameNote.isNotBlank()) {
                    Text(
                        text = note.nameNote,
                        color = backgroundColorWhite,   
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = if(note.state) "Эта заметка скрыта" else note.body,
                    color = backgroundColorWhite,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}