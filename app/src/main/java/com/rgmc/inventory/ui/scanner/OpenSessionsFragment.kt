package com.rgmc.inventory.ui.scanner

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentOpenSessionsBinding
import com.rgmc.inventory.ui.adapter.SessionAdapter
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OpenSessionsFragment : Fragment() {
    private var _binding: FragmentOpenSessionsBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()
    private lateinit var adapter: SessionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOpenSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SessionAdapter(
            onResume = { session ->
                vm.resumeSession(session)
                findNavController().navigate(R.id.action_openSessions_to_scannerInventory)
            },
            onDelete = { session -> vm.deleteSession(session) }
        )
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.openSessions.collectLatest { sessions ->
                adapter.submitList(sessions)
                binding.tvEmpty.isVisible = sessions.isEmpty()
                binding.rvSessions.isVisible = sessions.isNotEmpty()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
