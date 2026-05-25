package com.rgmc.inventory.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentMainBinding
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnScanner.setOnClickListener { findNavController().navigate(R.id.action_main_to_scannerSetup) }
        binding.btnProducts.setOnClickListener { findNavController().navigate(R.id.action_main_to_productMain) }
        binding.btnNotes.setOnClickListener { findNavController().navigate(R.id.action_main_to_notes) }
        binding.btnSigning.setOnClickListener { findNavController().navigate(R.id.action_main_to_signingList) }
        binding.btnExport.setOnClickListener { findNavController().navigate(R.id.action_main_to_export) }
        binding.btnOpenSessions.setOnClickListener { findNavController().navigate(R.id.action_main_to_openSessions) }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.openSessions.collectLatest { sessions ->
                val count = sessions.size
                binding.tvSessionCount.text = count.toString()
                binding.tvSessionSubtitle.text = if (count == 1) "1 active session" else "$count active sessions"
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
