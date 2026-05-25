package com.rgmc.inventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.data.local.entity.ScanningSessionEntity
import com.rgmc.inventory.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onResume: (ScanningSessionEntity) -> Unit,
    private val onDelete: (ScanningSessionEntity) -> Unit
) : ListAdapter<ScanningSessionEntity, SessionAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemSessionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(session: ScanningSessionEntity) {
            b.tvStoreName.text = session.storeName.ifEmpty { "Unknown Store" }
            b.tvCutOff.text = "Cut-off: ${session.cutOffDate}"
            val loc = buildString {
                if (session.locationName.isNotEmpty()) append("${session.locationName}, ")
                append("Rack ${session.rack}")
            }
            b.tvLocation.text = loc
            if (session.encoder.isNotEmpty()) {
                b.tvEncoder.isVisible = true
                b.tvEncoder.text = "Encoder: ${session.encoder}"
            } else {
                b.tvEncoder.isVisible = false
            }
            b.tvCreatedAt.text = "Started: ${formatDateTime(session.createdAt)}"
            b.tvLastModified.text = "Last activity: ${formatDateTime(session.lastModifiedAt)}"
            b.tvScannedCount.text = "${session.totalScanned}"
            b.root.setOnClickListener { onResume(session) }
            b.btnDelete.setOnClickListener { onDelete(session) }
        }

        private fun formatDateTime(raw: String): String {
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
                output.format(input.parse(raw) ?: return raw)
            } catch (e: Exception) { raw }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ScanningSessionEntity>() {
            override fun areItemsTheSame(a: ScanningSessionEntity, b: ScanningSessionEntity) = a.sessionId == b.sessionId
            override fun areContentsTheSame(a: ScanningSessionEntity, b: ScanningSessionEntity) = a == b
        }
    }
}
