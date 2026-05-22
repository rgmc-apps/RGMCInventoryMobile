package com.rgmc.inventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.data.local.entity.StoreInventoryPersonnelEntity
import com.rgmc.inventory.databinding.ItemPersonnelBinding

class PersonnelAdapter(
    private val onSign: (StoreInventoryPersonnelEntity) -> Unit,
    private val onDelete: (StoreInventoryPersonnelEntity) -> Unit
) : ListAdapter<StoreInventoryPersonnelEntity, PersonnelAdapter.VH>(DIFF) {
    inner class VH(val binding: ItemPersonnelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPersonnelBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvName.text = item.storePersonnel
            tvSigned.text = if (item.signature != null) "Signed" else "Not Signed"
            btnSign.setOnClickListener { onSign(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<StoreInventoryPersonnelEntity>() {
            override fun areItemsTheSame(o: StoreInventoryPersonnelEntity, n: StoreInventoryPersonnelEntity) = o.id == n.id
            override fun areContentsTheSame(o: StoreInventoryPersonnelEntity, n: StoreInventoryPersonnelEntity) = o.storePersonnel == n.storePersonnel && o.signature != null == (n.signature != null)
        }
    }
}
