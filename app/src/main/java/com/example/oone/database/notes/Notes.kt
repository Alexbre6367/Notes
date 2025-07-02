package com.example.oone.database.notes

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Keep
@Entity(tableName = "note")
data class Notes(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("noteId")
    var id: Int = 0,

    @ColumnInfo("noteBody")
    var body: String = "",

    @ColumnInfo("noteStatus")
    var status: Boolean = false,

    @ColumnInfo("notePassword")
    var state: Boolean = false,

    @ColumnInfo("lastEdited")
    var lastEdited: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo("ownerId")
    var ownerId: List<String> = listOf(),

    var aiStatus: Boolean = false,

    var nameNote: String = ""

) {
    fun toMap(ownerId: String): Map<String, Any?> = mapOf(
        "id" to id,
        "body" to body,
        "status" to status,
        "state" to state,
        "lastEdited" to lastEdited.toString(),
        "ownerId" to ownerId,
        "aiStatus" to aiStatus,
        "nameNote" to nameNote
    )
}