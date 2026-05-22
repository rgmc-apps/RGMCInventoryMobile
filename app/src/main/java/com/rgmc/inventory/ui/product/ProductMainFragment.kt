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

    private var currentBrands: List<BrandEntity> = emptyList()
    private var currentItemGroups: List<ItemGroupEntity> = emptyList()
    private var currentCategories: List<CategoryEntity> = emptyList()

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
        if (brands == currentBrands) return
        currentBrands = brands
        val items = listOf("Select Brand") + brands.map { it.name }
        val a = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        a.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerBrand.adapter = a
        binding.spinnerBrand.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onBrandSelected(brands[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupItemGroupSpinner(groups: List<ItemGroupEntity>) {
        if (groups == currentItemGroups) return
        currentItemGroups = groups
        val items = listOf("Select Item Group") + groups.map { it.name }
        val a = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        a.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerItemGroup.adapter = a
        binding.spinnerItemGroup.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onItemGroupSelected(groups[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupCategorySpinner(cats: List<CategoryEntity>) {
        if (cats == currentCategories) return
        currentCategories = cats
        val items = listOf("Select Category") + cats.map { it.name }
        val a = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
        a.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCategory.adapter = a
        binding.spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { if (pos > 0) vm.onCategorySelected(cats[pos - 1]) }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentBrands = emptyList()
        currentItemGroups = emptyList()
        currentCategories = emptyList()
    }
}
