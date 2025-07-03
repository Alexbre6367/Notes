package com.example.oone.database.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.oone.auth.SecureStorage
import com.example.oone.database.notes.NoteDao
import com.example.oone.database.notes.Notes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


class NotesRepository(private val notesDao: NoteDao?, private val secureStorage: SecureStorage) {

    private val firestore = Firebase.firestore
    private val notesCollection = firestore.collection("notes")
    val notesList: LiveData<List<Notes>> = notesDao?.getNotes() ?: MutableLiveData(emptyList())

    suspend fun updateNote(notes: Notes) {
        withContext(Dispatchers.IO) {
            notesDao?.upNote(notes)
            uploadNoteToFirestore(notes)
        }
    }

    suspend fun addNote(note: Notes) {
        withContext(Dispatchers.IO) {
            val newId = notesDao?.addNoteAndReturnId(note) ?: return@withContext
            note.id = newId.toInt()
            uploadNoteToFirestore(note)
        }
    }

    suspend fun insertIfNotExists(notes: Notes) { //проверка на существование в базе данных
        withContext(Dispatchers.IO) {
            val existingNote = notesDao?.getNoteById(notes.id)
            if (existingNote == null) {
                notesDao?.addNote(notes)
            } else {
                notesDao.upNote(notes)
            }
        }
    }

    suspend fun deleteNote(id: Int) {
        withContext(Dispatchers.IO) {
            notesDao?.deleteNote(id)
            notesCollection.document(id.toString()).delete()
        }
    }

    suspend fun deleteAllNotes(ownerId: String) {
        withContext(Dispatchers.IO) {
            notesDao?.deleteNotesByOwnerId(ownerId)

            notesCollection.whereEqualTo("ownerId", ownerId).get()
                .addOnSuccessListener { snapshots ->
                    snapshots.documents.forEach { doc ->
                        notesCollection.document(doc.id).delete()
                    }
                }
                .addOnFailureListener {
                    Log.e("MyLog", "Ошибка удаления заметок из Firestore", it)
                }
        }
    }

    private fun uploadNoteToFirestore(note: Notes) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        notesCollection.document(note.id.toString())
            .set(note.toMap(userId))
            .addOnSuccessListener {
                Log.d("Firestore", "Note uploaded: ${note.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Upload failed", e)
            }
    }

    fun listenToNotesChange(onChange: (DocumentChange.Type, Notes) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        notesCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MyLog", "Ошибка чтения", error)
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach { change ->
                    val data = change.document.data
                    val notes = try {
                        Notes(
                            id = (data["id"] as Long).toInt(),
                            body = data["body"] as String,
                            status = data["status"] as Boolean,
                            state = data["state"] as Boolean,
                            lastEdited = LocalDateTime.parse(data["lastEdited"] as String),
                            ownerId = (data["ownerId"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                            aiStatus = data["aiStatus"] as Boolean,
                            nameNote = data["nameNote"] as String
                        )
                    } catch (e: Exception) {
                        null
                    } ?: return@forEach
                    onChange(change.type, notes)
                }
            }
    }
}