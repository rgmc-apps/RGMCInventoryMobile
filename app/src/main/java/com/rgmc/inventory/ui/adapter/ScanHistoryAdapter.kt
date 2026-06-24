package com.rgmc.inventory.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.R
import com.rgmc.inventory.databinding.ItemScanHistoryBinding
import com.rgmc.inventory.util.resolveAttrColor

enum class ScanStatus { ACCEPTED, NOT_IN_NAV, REJECTED }

data class ScanHistoryItem(
    val id: Long = System.nanoTime(),
    val barcode: String,
    val format: String,
    val status: ScanStatus
)

class ScanHistoryAdapter : ListAdapter<ScanHistoryItem, ScanHistoryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemScanHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ScanHistoryItem) {
            b.tvHistoryBarcode.text = item.barcode
            b.tvHistoryFormat.text = item.format
            val attr = when (item.status) {
                ScanStatus.ACCEPTED -> R.attr.colorVariancePositive
                ScanStatus.NOT_IN_NAV -> R.attr.colorWarning
                ScanStatus.REJECTED -> R.attr.colorVarianceNegative
            }
            b.statusDot.backgroundTintList = ColorStateList.valueOf(
                b.root.context.resolveAttrColor(attr)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemScanHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ScanHistoryItem>() {
            override fun areItemsTheSame(a: ScanHistoryItem, b: ScanHistoryItem) = a.id == b.id
            override fun areContentsTheSame(a: ScanHistoryItem, b: ScanHistoryItem) = a == b
        }
    }
}
