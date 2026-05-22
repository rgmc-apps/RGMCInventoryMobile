package com.rgmc.inventory.ui.notes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentNotesBinding
import com.rgmc.inventory.ui.adapter.NoteAdapter
import com.rgmc.inventory.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val vm: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NoteAdapter(
            onClick = { note -> findNavController().navigate(R.id.action_notes_to_noteEntry, Bundle().apply { putInt("noteId", note.id) }) },
            onDelete = { note -> vm.deleteNote(note) }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.notes.collectLatest { adapter.submitList(it) }
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_notes_to_noteEntry, Bundle().apply { putInt("noteId", 0) })
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
