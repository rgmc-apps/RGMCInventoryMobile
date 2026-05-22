package com.rgmc.inventory.ui.product

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rgmc.inventory.databinding.FragmentProductListBinding
import com.rgmc.inventory.ui.adapter.ProductAdapter
import com.rgmc.inventory.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val vm: ProductViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ProductAdapter()
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch { vm.products.collectLatest { adapter.submitList(it) } }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean { vm.searchProducts(q ?: ""); return true }
            override fun onQueryTextChange(t: String?): Boolean { if (!t.isNullOrEmpty()) vm.searchProducts(t); return true }
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
