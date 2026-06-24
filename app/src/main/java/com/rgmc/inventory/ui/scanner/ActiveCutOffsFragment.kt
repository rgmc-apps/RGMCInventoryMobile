package com.rgmc.inventory.ui.scanner

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.FragmentActiveCutoffsBinding
import com.rgmc.inventory.ui.adapter.ActiveCutOffsAdapter
import com.rgmc.inventory.ui.viewmodel.ScannerViewModel
import com.rgmc.inventory.util.resolveAttrColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActiveCutOffsFragment : Fragment() {
    private var _binding: FragmentActiveCutoffsBinding? = null
    private val binding get() = _binding!!
    private val vm: ScannerViewModel by activityViewModels()
    private lateinit var adapter: ActiveCutOffsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveCutoffsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ActiveCutOffsAdapter()
        binding.rvCutoffs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCutoffs.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            requireContext().resolveAttrColor(R.attr.colorPrimaryLight)
        )
        binding.swipeRefresh.setOnRefreshListener { vm.loadActiveCutOffs() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.activeCutOffsLoading.collectLatest { loading ->
                binding.swipeRefresh.isRefreshing = loading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.activeCutOffs.collectLatest { items ->
                adapter.submitList(items)
                binding.tvCutoffCount.text = items.size.toString()
                binding.tvEmpty.isVisible = items.isEmpty()
                binding.rvCutoffs.isVisible = items.isNotEmpty()
            }
        }

        vm.loadActiveCutOffs()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
