package com.rgmc.inventory.ui.signing

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentSigningListBinding
import com.rgmc.inventory.ui.adapter.PersonnelAdapter
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import com.rgmc.inventory.ui.viewmodel.SigningViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SigningListFragment : Fragment() {
    private var _binding: FragmentSigningListBinding? = null
    private val binding get() = _binding!!
    private val scannerVm: ScannerViewModel by activityViewModels()
    private val signingVm: SigningViewModel by activityViewModels()
    private lateinit var adapter: PersonnelAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSigningListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = scannerVm.setupState.value
        signingVm.init(state.selectedStore?.storeId ?: 0, state.selectedCutOff?.cutOffDate ?: "", state.encoder)
        adapter = PersonnelAdapter(
            onSign = { p -> findNavController().navigate(R.id.action_signingList_to_signing, Bundle().apply { putInt("personnelId", p.id); putString("name", p.storePersonnel) }) },
            onDelete = { p -> signingVm.deletePersonnel(p) }
        )
        binding.rvPersonnel.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPersonnel.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch { signingVm.personnel.collectLatest { adapter.submitList(it) } }
        binding.fabAdd.setOnClickListener {
            val input = EditText(requireContext())
            AlertDialog.Builder(requireContext()).setTitle("Add Personnel").setView(input)
                .setPositiveButton("Add") { _, _ -> signingVm.addPersonnel(input.text.toString()) }
                .setNegativeButton("Cancel", null).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
