package com.example.oone.database.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.oone.auth.SecureStorage
import com.example.oone.database.ai.Gemini
import com.example.oone.database.notes.Notes
import com.example.oone.database.notes.NotesRoomDatabase
import com.example.oone.database.repositories.NotesRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    val notesList: LiveData<List<Notes>>
    private val repository: NotesRepository

    private val secureStorage = SecureStorage(application)
    private val _userEmail = MutableStateFlow(secureStorage.getEmail() ?: "")
    val userEmail = _userEmail.asStateFlow()

    private val _userPassword = MutableStateFlow(secureStorage.getPassword() ?: "")
    val userPassword = _userPassword.asStateFlow()

    init {
        val noteDb = NotesRoomDatabase.getInstance(application)
        val noteDao = noteDb?.noteDao()
        repository = NotesRepository(noteDao, secureStorage)
        notesList = repository.notesList

        loadNotesFromFirestore()
    }

    fun addNote(note: Notes) {
        viewModelScope.launch {
            repository.addNote(note)
        }
    }

    fun updateNote(note: Notes){
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    private val _selectedNoteId = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteId = _selectedNoteId.asStateFlow()
    fun toggleNoteSelection(noteId: Int) {
        _selectedNoteId.value = if(noteId in _selectedNoteId.value) {
            selectedNoteId.value - noteId
        } else {
            _selectedNoteId.value + noteId
        }
    }

    fun toggleNoteAll() {
        _selectedNoteId.value = notesList.value?.map { notes -> notes.id }?.toSet()?: emptySet()
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            _selectedNoteId.value.forEach { id ->
                repository.deleteNote(id)
            }
            _selectedNoteId.value = emptySet()
        }
    }

    fun clearSelection(){
        _selectedNoteId.value = emptySet()
    }

    fun deleteNote(id: Int){
        viewModelScope.launch {
            repository.deleteNote(id)
            _selectedNoteId.value = _selectedNoteId.value - id
        }
    }

    fun deleteUserAndNotes(auth: FirebaseAuth, email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            repository.deleteAllNotes(userId)

            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
                if(it.isSuccessful) {
                    auth.currentUser?.delete()?.addOnCompleteListener { result ->
                        onComplete(result.isSuccessful)
                    }
                } else {
                    onComplete(false)
                }
            }
        }
    }

    fun loadNotesFromFirestore() {
        repository.listenToNotesChange { changeType, notes ->
            viewModelScope.launch {
                when (changeType) {
                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                        repository.insertIfNotExists(notes)
                    }
                    DocumentChange.Type.REMOVED -> {
                        withContext(Dispatchers.IO) {
                            repository.notesDao?.deleteNote(notes.id)
                        }
                    }
                }
            }
        }
    }

    fun setUserCredentials(email: String, password: String) {
        _userEmail.value = email
        _userPassword.value = password
        secureStorage.saveCredentials(email, password)
    }

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    private val _errorLog = Channel<String>(Channel.BUFFERED)
    val errorLog: Flow<String> = _errorLog.receiveAsFlow()
    fun analyze(text: String) {
        viewModelScope.launch {
            val result = Gemini.analyze(text)
            if(result == null) {
                _errorLog.send("Ошибка обработки")
            } else {
                _analysisResult.value = result
            }
        }
    }

    fun clearAi() {
        _analysisResult.value = null
    }
}
