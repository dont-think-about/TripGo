package com.nbcamp.tripgo.view.tour.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.databinding.TourRecyclerviewItemBinding

class TourAdapter(
    private val onClickItem: (FestivalItem) -> Unit
) : ListAdapter<FestivalItem, TourAdapter.TourViewHolder>(TourDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding =
            TourRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding, onClickItem)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class TourViewHolder(
        private val binding: TourRecyclerviewItemBinding,
        private val onClickItem: (FestivalItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FestivalItem) {

            itemView.setOnClickListener {

                onClickItem(item)
            }

            with(binding) {
                tourTitle.text = item.title
                tourSubtitle.text = ""
                tourAddress.text = item.addr1

                myImage.load(item.firstimage)
            }
        }
    }

    companion object {
        private val TourDiffCallback = object : DiffUtil.ItemCallback<FestivalItem>() {
            override fun areItemsTheSame(oldItem: FestivalItem, newItem: FestivalItem): Boolean {
                return oldItem.contentid == newItem.contentid  // contentid를 고유 식별자로 사용하여 비교
            }

            override fun areContentsTheSame(oldItem: FestivalItem, newItem: FestivalItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
