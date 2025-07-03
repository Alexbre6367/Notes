package com.example.oone.database.notes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.oone.database.converters.Converters
import com.example.oone.database.converters.ConvertersRoom

@Database(entities = [Notes::class], version = 10)
@TypeConverters(Converters::class, ConvertersRoom::class)
abstract class NotesRoomDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private var INSTANCE: NotesRoomDatabase? = null

        fun getInstance(context: Context): NotesRoomDatabase? {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NotesRoomDatabase::class.java,
                        "notesdb"
                    ).addMigrations(
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10
                    ).build()
                }
                return instance
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE note ADD COLUMN aiStatus INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_8_9= object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE note ADD COLUMN nameNote  TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_new (
                    noteId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    noteBody TEXT NOT NULL,
                    noteStatus INTEGER NOT NULL,
                    notePassword INTEGER NOT NULL,
                    lastEdited TEXT NOT NULL,
                    ownerId TEXT NOT NULL,
                    aiStatus INTEGER NOT NULL,
                    nameNote TEXT NOT NULL
                    )
                    """.trimIndent())

                db.execSQL(
                    """
                    INSERT INTO note_new (noteId, noteBody, noteStatus, notePassword, lastEdited, ownerId, aiStatus, nameNote)
                    SELECT noteId, noteBody, noteStatus, notePassword, lastEdited, ownerId, aiStatus, nameNote FROM note 
                    """.trimIndent())

                db.execSQL("DROP TABLE note")
                db.execSQL("ALTER TABLE note_new RENAME TO note")
            }
        }
    }
}