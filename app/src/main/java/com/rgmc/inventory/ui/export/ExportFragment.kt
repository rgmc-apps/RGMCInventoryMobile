package com.rgmc.inventory.ui.export

import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rgmc.inventory.R
import com.rgmc.inventory.data.local.entity.StoreInventoryCutOffEntity
import com.rgmc.inventory.databinding.FragmentExportBinding
import com.rgmc.inventory.ui.viewmodel.ExportViewModel
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExportFragment : Fragment() {
    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!
    private val exportVm: ExportViewModel by viewModels()
    private val scannerVm: ScannerViewModel by activityViewModels()

    private var currentCutOffs: List<StoreInventoryCutOffEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = scannerVm.setupState.value
        exportVm.loadForStore(state.selectedStore?.storeId ?: 0, state.encoder)

        viewLifecycleOwner.lifecycleScope.launch {
            exportVm.state.collectLatest { s ->
                binding.progressBar.isVisible = s.isLoading
                setupCutOffSpinner(s.cutOffs)
                s.message?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
            }
        }

        binding.btnExport.setOnClickListener {
            val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
            exportVm.exportAll(deviceId)
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
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos > 0) exportVm.onCutOffSelected(cutOffs[pos - 1])
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentCutOffs = emptyList()
    }
}
