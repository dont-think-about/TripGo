package com.nbcamp.tripgo.view.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.databinding.ItemSearchBinding

class AttractionAdapter(private val sContext: Context) :
    RecyclerView.Adapter<AttractionAdapter.SearchItemViewHolder>() {
    var items = arrayListOf<KeywordSearchEntity>()

    fun clearItem() {
        items.clear()
        notifyDataSetChanged()
    }
    fun additem(list: List<KeywordSearchEntity>) {
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
        val binding = ItemSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class SearchItemViewHolder(binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var title: TextView = binding.searchVideo
        var url: TextView = binding.videoName
        var dateTime: TextView = binding.dateTime
        var id: View = binding.root  // 혹시나 다른 변수를 받아올 때 사용할 수 있음

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(item: KeywordSearchEntity) {
            title.text = item.longitude
            url.text = item.latitude
            dateTime.text = item.contentId
        }

        override fun onClick(v: View?) {
            val position = adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
            // 아이템을 클릭했을 때 수행할 작업을 여기에 추가
        }
    }
}
