package com.rgmc.inventory.ui.scanner

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rgmc.inventory.R
import com.rgmc.inventory.data.local.entity.*
import com.rgmc.inventory.databinding.FragmentScannerSetupBinding
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScannerSetupFragment : Fragment() {
    private var _binding: FragmentScannerSetupBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.loadInitialData()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.setupState.collectLatest { state ->
                binding.progressBar.isVisible = state.isLoading
                setupBrandSpinner(state.brands)
                setupCustomerSpinner(state.customers)
                setupStoreSpinner(state.stores)
                setupCoordinatorSpinner(state.coordinators)
                setupCutOffSpinner(state.cutOffs)
                setupLocationSpinner(state.locations)
                state.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
            }
        }

        binding.etEncoder.setOnFocusChangeListener { _, _ -> vm.onEncoderChanged(binding.etEncoder.text.toString()) }
        binding.etRack.setOnFocusChangeListener { _, _ -> vm.onRackChanged(binding.etRack.text.toString().toIntOrNull() ?: 1) }

        binding.btnEnter.setOnClickListener {
            val state = vm.setupState.value
            if (state.selectedStore == null || state.selectedCutOff == null) {
                Toast.makeText(requireContext(), "Please select Store and Cut-Off Date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.onEncoderChanged(binding.etEncoder.text.toString())
            vm.onRackChanged(binding.etRack.text.toString().toIntOrNull() ?: 1)
            vm.saveSetting()
            vm.loadNavList(state.selectedStore.storeId)
            findNavController().navigate(R.id.action_scannerSetup_to_scannerInventory)
        }
    }

    private fun setupBrandSpinner(brands: List<BrandEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, brands.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBrand.adapter = adapter
        binding.spinnerBrand.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (brands.isNotEmpty()) vm.onBrandSelected(brands[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCustomerSpinner(customers: List<CustomerEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customers.map { it.customerName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCustomer.adapter = adapter
        binding.spinnerCustomer.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (customers.isNotEmpty()) vm.onCustomerSelected(customers[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupStoreSpinner(stores: List<CustomerStoreEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, stores.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStore.adapter = adapter
        binding.spinnerStore.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (stores.isNotEmpty()) vm.onStoreSelected(stores[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCoordinatorSpinner(coordinators: List<BrandCoordinatorEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, coordinators.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCoordinator.adapter = adapter
        binding.spinnerCoordinator.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (coordinators.isNotEmpty()) vm.onCoordinatorSelected(coordinators[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCutOffSpinner(cutOffs: List<StoreInventoryCutOffEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cutOffs.map { it.cutOffDate })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCutOff.adapter = adapter
        binding.spinnerCutOff.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (cutOffs.isNotEmpty()) vm.onCutOffSelected(cutOffs[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupLocationSpinner(locations: List<StoreInventoryLocationEntity>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations.map { it.locationName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter
        binding.spinnerLocation.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (locations.isNotEmpty()) vm.onLocationSelected(locations[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
