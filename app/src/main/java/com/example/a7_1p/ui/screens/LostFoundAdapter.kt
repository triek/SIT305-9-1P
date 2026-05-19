package com.example.a7_1p.ui.screens

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a7_1p.R
import com.example.a7_1p.data.DateTimeFormatterUtil
import com.example.a7_1p.data.LostFoundItem

class LostFoundAdapter(
    private val onItemClick: (LostFoundItem) -> Unit
) : RecyclerView.Adapter<LostFoundAdapter.ItemViewHolder>() {

    private val items = mutableListOf<LostFoundItem>()

    fun submitList(newItems: List<LostFoundItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val preview: ImageView = itemView.findViewById(R.id.itemImagePreview)
        private val name: TextView = itemView.findViewById(R.id.itemName)
        private val type: TextView = itemView.findViewById(R.id.itemType)
        private val category: TextView = itemView.findViewById(R.id.itemCategory)
        private val date: TextView = itemView.findViewById(R.id.itemDate)

        fun bind(item: LostFoundItem) {
            name.text = item.name
            type.text = "Type: ${item.type}"
            category.text = "Category: ${item.category}"
            date.text = "Posted: ${DateTimeFormatterUtil.formatForList(item.createdAtMillis)}"

            if (item.imageUri.isNotBlank() && item.imageUri != "selected-image-placeholder") {
                preview.setImageURI(Uri.parse(item.imageUri))
                if (preview.drawable == null) {
                    preview.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                preview.setImageResource(R.drawable.ic_launcher_foreground)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
