package com.rgmc.inventory.data.repository

import com.rgmc.inventory.data.local.AppDatabase
import com.rgmc.inventory.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val db: AppDatabase) {
    fun getAllNotes(): Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    suspend fun getNoteById(id: Int) = db.noteDao().getNoteById(id)
    suspend fun insert(note: NoteEntity) = db.noteDao().insert(note)
    suspend fun update(note: NoteEntity) = db.noteDao().update(note)
    suspend fun delete(note: NoteEntity) = db.noteDao().delete(note)
}
