package com.rgmc.inventory.ui.scanner

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rgmc.inventory.R
import com.rgmc.inventory.data.local.entity.StoreInventoryLocationEntity
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
    private var currentLocations: List<StoreInventoryLocationEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = InventoryAdapter()
        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventory.adapter = adapter

        // Pre-fill rack from current ViewModel state (handles resumed sessions)
        binding.etRack.setText(vm.setupState.value.rack.toString())
        binding.etRack.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) vm.onRackChanged(binding.etRack.text.toString().toIntOrNull() ?: 1)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.setupState.collectLatest { state ->
                setupLocationSpinner(state.locations)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.navList.collectLatest { list ->
                adapter.submitList(list)
                val totalNav = list.sumOf { it.qty }
                val totalScanned = list.sumOf { it.actualQty }
                binding.tvNavQty.text = "$totalNav"
                binding.tvScannedQty.text = "$totalScanned"
                binding.tvVariance.text = "${totalScanned - totalNav}"
                binding.progressBar.isVisible = false
                binding.tvEmpty.isVisible = list.isEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.message.collect { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show() }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { vm.searchInventory(query ?: ""); return true }
            override fun onQueryTextChange(newText: String?): Boolean { if (newText.isNullOrEmpty()) vm.clearSearch(); return true }
        })

        binding.btnScan.setOnClickListener { findNavController().navigate(R.id.action_scannerInventory_to_barcodeScanner) }

        binding.btnClearScans.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Scans")
                .setMessage("Remove all scanned quantities for this store and cut-off? This cannot be undone.")
                .setPositiveButton("Clear") { _, _ -> vm.clearScans() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnImport.setOnClickListener {
            val state = vm.setupState.value
            val storeId = state.selectedStore?.storeId ?: return@setOnClickListener
            val cutOff = state.selectedCutOff?.cutOffDate ?: return@setOnClickListener
            binding.progressBar.isVisible = true
            vm.importData(storeId, cutOff)
        }
    }

    private fun setupLocationSpinner(locations: List<StoreInventoryLocationEntity>) {
        if (locations == currentLocations) return
        currentLocations = locations
        val items = listOf("Select Location") + locations.map { it.locationName }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter

        val currentSelected = vm.setupState.value.selectedLocation
        if (currentSelected != null) {
            val idx = locations.indexOfFirst { it.locationId == currentSelected.locationId }
            if (idx >= 0) binding.spinnerLocation.setSelection(idx + 1)
        } else {
            val stockIdx = locations.indexOfFirst { it.locationName.contains("stock", ignoreCase = true) }
            if (stockIdx >= 0) {
                binding.spinnerLocation.setSelection(stockIdx + 1)
                vm.onLocationSelected(locations[stockIdx])
            }
        }

        binding.spinnerLocation.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos > 0) vm.onLocationSelected(locations[pos - 1])
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onPause() { super.onPause(); vm.updateSessionActivity() }

    override fun onDestroyView() {
        super.onDestroyView()
        currentLocations = emptyList()
        _binding = null
    }
}
