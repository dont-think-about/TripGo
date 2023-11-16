package com.nbcamp.tripgo.view.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.databinding.ItemMainFestivalCardBinding
import com.nbcamp.tripgo.view.App

class FestivalViewPagerAdapter :
    ListAdapter<FestivalEntity, FestivalViewPagerAdapter.FestivalViewPagerViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewPagerViewHolder {
        return FestivalViewPagerViewHolder(
            ItemMainFestivalCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FestivalViewPagerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FestivalViewPagerViewHolder(
        private val binding: ItemMainFestivalCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: FestivalEntity) = with(binding) {
            itemTitleTextView.text = model.title
            "${model.startDate} ~ ${model.endDate}".also { itemDescriptionTextView.text = it }
            when {
                model.imageUrl.isEmpty() -> {
                    itemMainImageView.scaleType = ImageView.ScaleType.FIT_XY
                    itemMainImageView.load(R.drawable.icon_no_image)
                    itemTitleTextView.setTextColor(Color.WHITE)
                    itemDescriptionTextView.setTextColor(Color.WHITE)
                }
                else -> {
                    itemMainImageView.scaleType = ImageView.ScaleType.FIT_XY
                    itemMainImageView.load(model.imageUrl, App.imageLoader) {
                        placeholder(R.drawable.icon_main_logo)
                        memoryCachePolicy(CachePolicy.ENABLED)
                        diskCachePolicy(CachePolicy.ENABLED)
                    }
                    itemTitleTextView.setTextColor(Color.WHITE)
                    itemDescriptionTextView.setTextColor(Color.WHITE)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FestivalEntity>() {
            override fun areItemsTheSame(
                oldItem: FestivalEntity,
                newItem: FestivalEntity
            ): Boolean = oldItem.contentId == newItem.contentId

            override fun areContentsTheSame(
                oldItem: FestivalEntity,
                newItem: FestivalEntity
            ): Boolean = oldItem == newItem
        }
    }
}
