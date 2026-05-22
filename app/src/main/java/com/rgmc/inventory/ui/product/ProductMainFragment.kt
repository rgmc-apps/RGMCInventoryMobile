package com.rgmc.inventory.ui.product

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
import com.rgmc.inventory.databinding.FragmentProductMainBinding
import com.rgmc.inventory.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductMainFragment : Fragment() {
    private var _binding: FragmentProductMainBinding? = null
    private val binding get() = _binding!!
    private val vm: ProductViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.loadBrands()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collectLatest { state ->
                binding.progressBar.isVisible = state.isLoading
                setupBrandSpinner(state.brands)
                setupItemGroupSpinner(state.itemGroups)
                setupCategorySpinner(state.categories)
            }
        }

        binding.btnEnter.setOnClickListener {
            val cat = vm.state.value.selectedCategory ?: run { Toast.makeText(requireContext(), "Select a category", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            vm.loadProducts(cat.categoryId)
            findNavController().navigate(R.id.action_productMain_to_productList)
        }
    }

    private fun setupBrandSpinner(brands: List<BrandEntity>) {
        val a = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, brands.map { it.name })
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBrand.adapter = a
        binding.spinnerBrand.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (brands.isNotEmpty()) vm.onBrandSelected(brands[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupItemGroupSpinner(groups: List<ItemGroupEntity>) {
        val a = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, groups.map { it.name })
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerItemGroup.adapter = a
        binding.spinnerItemGroup.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (groups.isNotEmpty()) vm.onItemGroupSelected(groups[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCategorySpinner(cats: List<CategoryEntity>) {
        val a = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cats.map { it.name })
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = a
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (cats.isNotEmpty()) vm.onCategorySelected(cats[pos]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
