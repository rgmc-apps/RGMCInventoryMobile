package com.rgmc.inventory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.data.local.entity.NoteEntity
import com.rgmc.inventory.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (NoteEntity) -> Unit,
    private val onDelete: (NoteEntity) -> Unit
) : ListAdapter<NoteEntity, NoteAdapter.VH>(DIFF) {
    inner class VH(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvTitle.text = item.title
            tvDate.text = item.date
            root.setOnClickListener { onClick(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<NoteEntity>() {
            override fun areItemsTheSame(o: NoteEntity, n: NoteEntity) = o.id == n.id
            override fun areContentsTheSame(o: NoteEntity, n: NoteEntity) = o == n
        }
    }
}
