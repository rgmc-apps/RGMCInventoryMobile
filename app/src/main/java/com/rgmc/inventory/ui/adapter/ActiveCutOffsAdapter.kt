package com.rgmc.inventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.databinding.ItemActiveCutoffBinding
import com.rgmc.inventory.ui.viewmodel.ActiveCutOffItem

class ActiveCutOffsAdapter : ListAdapter<ActiveCutOffItem, ActiveCutOffsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemActiveCutoffBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ActiveCutOffItem) {
            b.tvStoreName.text = item.storeName.ifEmpty { "Store #${item.storeId}" }
            b.tvCustomerName.text = item.customerName
            b.tvCutOffDate.text = "Cut-off: ${item.cutOffDate}"
            b.tvCreateBy.text = if (item.createBy.isNotEmpty()) "By: ${item.createBy}" else ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemActiveCutoffBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ActiveCutOffItem>() {
            override fun areItemsTheSame(a: ActiveCutOffItem, b: ActiveCutOffItem) = a.storeCutOff == b.storeCutOff
            override fun areContentsTheSame(a: ActiveCutOffItem, b: ActiveCutOffItem) = a == b
        }
    }
}
