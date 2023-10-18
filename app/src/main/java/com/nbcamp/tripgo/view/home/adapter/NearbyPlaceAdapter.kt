package com.nbcamp.tripgo.view.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity
import com.nbcamp.tripgo.databinding.ItemMainTourCardBinding


class NearbyPlaceAdapter(
    private val context: Context,
    private val onClickItem: (String) -> Unit
) :
    ListAdapter<NearbyPlaceEntity, NearbyPlaceAdapter.NearbyPlaceViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyPlaceViewHolder {
        return NearbyPlaceViewHolder(
            ItemMainTourCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClickItem
        )
    }

    override fun onBindViewHolder(holder: NearbyPlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NearbyPlaceViewHolder(
        private val binding: ItemMainTourCardBinding,
        private val onClickItem: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: NearbyPlaceEntity) = with(binding) {
            val distance = model.distance.toDouble()
            val outputDistance = if (distance < 1000) {
                context.getString(R.string.in_1km)
            } else {
                "${(distance / 1000).toInt() + 1}" + context.getString(R.string.in_km)
            }
            itemTitleTextView.text = model.title
            itemDescriptionTextView.text = outputDistance
            itemMainImageView.load(model.imageUrl) {
                placeholder(R.drawable.icon_camping)
            }
            itemView.setOnClickListener {
                onClickItem(model.contentId)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NearbyPlaceEntity>() {
            override fun areItemsTheSame(
                oldItem: NearbyPlaceEntity,
                newItem: NearbyPlaceEntity
            ): Boolean = oldItem.contentId == newItem.contentId


            override fun areContentsTheSame(
                oldItem: NearbyPlaceEntity,
                newItem: NearbyPlaceEntity
            ): Boolean = oldItem == newItem
        }
    }
}

