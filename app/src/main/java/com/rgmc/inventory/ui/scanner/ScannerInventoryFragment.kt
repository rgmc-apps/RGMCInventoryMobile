package com.rgmc.inventory.ui.scanner

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentScannerInventoryBinding
import com.rgmc.inventory.ui.adapter.InventoryAdapter
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScannerInventoryFragment : Fragment() {
    private var _binding: FragmentScannerInventoryBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = InventoryAdapter()
        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.navList.collectLatest { list ->
                adapter.submitList(list)
                val totalNav = list.sumOf { it.qty }
                val totalScanned = list.sumOf { it.actualQty }
                binding.tvNavQty.text = "$totalNav"
                binding.tvScannedQty.text = "$totalScanned"
                binding.tvVariance.text = "${totalScanned - totalNav}"
                binding.progressBar.isVisible = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.message.collect { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show() }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { vm.searchInventory(query ?: ""); return true }
            override fun onQueryTextChange(newText: String?): Boolean { if (newText.isNullOrEmpty()) { val storeId = vm.setupState.value.selectedStore?.storeId ?: return true; vm.loadNavList(storeId) }; return true }
        })

        binding.btnScan.setOnClickListener { findNavController().navigate(R.id.action_scannerInventory_to_barcodeScanner) }

        binding.btnImport.setOnClickListener {
            val state = vm.setupState.value
            val storeId = state.selectedStore?.storeId ?: return@setOnClickListener
            val cutOff = state.selectedCutOff?.cutOffDate ?: return@setOnClickListener
            binding.progressBar.isVisible = true
            vm.importData(storeId, cutOff)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
