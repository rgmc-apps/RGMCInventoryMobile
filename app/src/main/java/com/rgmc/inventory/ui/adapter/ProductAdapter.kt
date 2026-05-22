package com.rgmc.inventory.ui.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgmc.inventory.data.local.entity.ProductImageEntity
import com.rgmc.inventory.databinding.ItemProductBinding

class ProductAdapter : ListAdapter<ProductImageEntity, ProductAdapter.VH>(DIFF) {
    inner class VH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvStockNumber.text = item.stockNumber
            tvDescription.text = item.description
            tvPrice.text = "₱ ${"%.2f".format(item.price)}"
            if (item.imageData != null) {
                val bmp = BitmapFactory.decodeByteArray(item.imageData, 0, item.imageData.size)
                ivProduct.setImageBitmap(bmp)
            } else {
                ivProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ProductImageEntity>() {
            override fun areItemsTheSame(o: ProductImageEntity, n: ProductImageEntity) = o.stockNumber == n.stockNumber
            override fun areContentsTheSame(o: ProductImageEntity, n: ProductImageEntity) = o == n
        }
    }
}
