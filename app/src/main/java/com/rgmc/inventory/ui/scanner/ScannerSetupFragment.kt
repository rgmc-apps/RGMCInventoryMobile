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

    private var currentBrands: List<BrandEntity> = emptyList()
    private var currentCustomers: List<CustomerEntity> = emptyList()
    private var currentStores: List<CustomerStoreEntity> = emptyList()
    private var currentCoordinators: List<BrandCoordinatorEntity> = emptyList()
    private var currentCutOffs: List<StoreInventoryCutOffEntity> = emptyList()
    private var currentLocations: List<StoreInventoryLocationEntity> = emptyList()

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
                val noCutOffs = !state.isCutOffLoading && state.selectedStore != null && state.cutOffs.isEmpty()
                binding.spinnerCutOff.isVisible = !state.isCutOffLoading && !noCutOffs
                binding.cutOffLoadingRow.isVisible = state.isCutOffLoading
                binding.cutOffEmptyNotice.isVisible = noCutOffs
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
            vm.createSession()
            vm.loadNavList(state.selectedStore.storeId)
            findNavController().navigate(R.id.action_scannerSetup_to_scannerInventory)
        }
    }

    private fun setupBrandSpinner(brands: List<BrandEntity>) {
        if (brands == currentBrands) return
        currentBrands = brands
        val items = listOf("Select Brand") + brands.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerBrand.adapter = adapter
        binding.spinnerBrand.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onBrandSelected(brands[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCustomerSpinner(customers: List<CustomerEntity>) {
        if (customers == currentCustomers) return
        currentCustomers = customers
        val items = listOf("Select Customer") + customers.map { it.customerName }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCustomer.adapter = adapter
        binding.spinnerCustomer.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onCustomerSelected(customers[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupStoreSpinner(stores: List<CustomerStoreEntity>) {
        if (stores == currentStores) return
        currentStores = stores
        val items = listOf("Select Store") + stores.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerStore.adapter = adapter
        binding.spinnerStore.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onStoreSelected(stores[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCoordinatorSpinner(coordinators: List<BrandCoordinatorEntity>) {
        if (coordinators == currentCoordinators) return
        currentCoordinators = coordinators
        val items = listOf("Select Coordinator") + coordinators.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCoordinator.adapter = adapter
        binding.spinnerCoordinator.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onCoordinatorSelected(coordinators[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCutOffSpinner(cutOffs: List<StoreInventoryCutOffEntity>) {
        if (cutOffs == currentCutOffs) return
        currentCutOffs = cutOffs
        val items = listOf("Select Cut-Off Date") + cutOffs.map { it.cutOffDate }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCutOff.adapter = adapter
        binding.spinnerCutOff.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onCutOffSelected(cutOffs[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupLocationSpinner(locations: List<StoreInventoryLocationEntity>) {
        if (locations == currentLocations) return
        currentLocations = locations
        val items = listOf("Select Location") + locations.map { it.locationName }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter
        binding.spinnerLocation.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onLocationSelected(locations[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentBrands = emptyList()
        currentCustomers = emptyList()
        currentStores = emptyList()
        currentCoordinators = emptyList()
        currentCutOffs = emptyList()
        currentLocations = emptyList()
    }
}
