package com.rgmc.inventory.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.rgmc.inventory.RGMCApp
import com.rgmc.inventory.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as RGMCApp).noteRepository
    val notes = repo.getAllNotes().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveNote(id: Int, title: String, text: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            if (id == 0) repo.insert(NoteEntity(title = title, text = text, date = date))
            else repo.update(NoteEntity(id, title, text, date))
        }
    }

    fun deleteNote(note: NoteEntity) { viewModelScope.launch { repo.delete(note) } }
}
