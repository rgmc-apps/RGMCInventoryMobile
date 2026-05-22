package com.rgmc.inventory.ui.notes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rgmc.inventory.databinding.FragmentNoteEntryBinding
import com.rgmc.inventory.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class NoteEntryFragment : Fragment() {
    private var _binding: FragmentNoteEntryBinding? = null
    private val binding get() = _binding!!
    private val vm: NoteViewModel by viewModels()
    private var noteId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNoteEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteId = arguments?.getInt("noteId") ?: 0
        if (noteId != 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                val note = vm.notes.value.find { it.id == noteId }
                note?.let { binding.etTitle.setText(it.title); binding.etText.setText(it.text) }
            }
        }
        binding.btnSave.setOnClickListener {
            vm.saveNote(noteId, binding.etTitle.text.toString(), binding.etText.text.toString())
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
