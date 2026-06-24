package com.rgmc.inventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.R
import com.rgmc.inventory.data.local.entity.StoreInventoryNAVEntity
import com.rgmc.inventory.databinding.ItemInventoryBinding
import com.rgmc.inventory.util.resolveAttrColor

class InventoryAdapter : ListAdapter<StoreInventoryNAVEntity, InventoryAdapter.VH>(DIFF) {
    inner class VH(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvBarcode.text = item.barcode
            tvDescription.text = item.description
            tvNavQty.text = "${item.qty}"
            tvScanned.text = "${item.actualQty}"
            tvVariance.text = "${item.variance}"
            val attrColor = when {
                item.variance < 0 -> R.attr.colorVarianceNegative
                item.variance > 0 -> R.attr.colorVariancePositive
                else -> R.attr.colorVarianceZero
            }
            tvVariance.setTextColor(root.context.resolveAttrColor(attrColor))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<StoreInventoryNAVEntity>() {
            override fun areItemsTheSame(o: StoreInventoryNAVEntity, n: StoreInventoryNAVEntity) = o.id == n.id
            override fun areContentsTheSame(o: StoreInventoryNAVEntity, n: StoreInventoryNAVEntity) = o == n
        }
    }
}
