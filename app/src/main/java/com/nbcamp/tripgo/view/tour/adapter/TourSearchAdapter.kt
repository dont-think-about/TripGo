package com.nbcamp.tripgo.view.tour.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.databinding.TourRecyclerviewItemBinding

class TourSearchAdapter(
    private val onClickItem:(KeywordItem) -> Unit
) : ListAdapter<KeywordItem, TourSearchAdapter.TourViewHolder>(TourDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding = TourRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding , onClickItem)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class TourViewHolder(private val binding: TourRecyclerviewItemBinding ,
    private val onClickItem: (KeywordItem) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: KeywordItem) {

            itemView.setOnClickListener {

                onClickItem(item)
            }

            with(binding) {
                tourTitle.text = item.title
                tourSubtitle.text = item.contentid
                tourAddress.text = item.addr1

               myImage.load(item.firstimage)
            }
        }
    }

    companion object {
        private val TourDiffCallback = object : DiffUtil.ItemCallback<KeywordItem>() {
            override fun areItemsTheSame(oldItem: KeywordItem, newItem: KeywordItem): Boolean {
                return oldItem.contentid == newItem.contentid  // contentid를 고유 식별자로 사용하여 비교
            }

            override fun areContentsTheSame(oldItem: KeywordItem, newItem: KeywordItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
