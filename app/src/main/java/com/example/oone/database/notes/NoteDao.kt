package com.example.oone.database.notes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao{
    @Query("Select * FROM note")
    fun getNotes(): LiveData<List<Notes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNote(notes: Notes)

    @Update
    fun upNote(notes: Notes)

    @Query("Delete from note where noteId = :id")
    fun deleteNote(id: Int)

    @Insert
    suspend fun addNoteAndReturnId(note: Notes): Long

    @Query("SELECT * FROM note WHERE noteId = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Int): Notes?

    @Query("DELETE FROM note WHERE ownerId = :ownerId")
    suspend fun deleteNotesByOwnerId(ownerId: String)
}