package com.nbcamp.tripgo.view.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ItemMainTourCardBinding
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.home.valuetype.ProvincePlaceEntity
import kotlin.math.floor

class ProvincePlaceListAdapter(
    private val context: Context,
    private val onClickItem: (ProvincePlaceEntity) -> Unit
) : ListAdapter<ProvincePlaceEntity, ProvincePlaceListAdapter.ProvincePlaceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvincePlaceViewHolder {
        return ProvincePlaceViewHolder(
            ItemMainTourCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClickItem
        )
    }

    override fun onBindViewHolder(holder: ProvincePlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProvincePlaceViewHolder(
        private val binding: ItemMainTourCardBinding,
        private val onClickItem: (ProvincePlaceEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ProvincePlaceEntity) = with(binding) {
            val str = ((floor(model.tourListCount.toDouble() / 100)) * 100).toInt()
            itemView.setOnClickListener {
                onClickItem(model)
            }
            itemTitleTextView.text = model.name
            "$str" + context.getString(R.string.many_tour_place).also {
                itemDescriptionTextView.text = it
            }
            itemMainImageView.load(model.imageUrl, App.imageLoader) {
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProvincePlaceEntity>() {
            override fun areItemsTheSame(
                oldItem: ProvincePlaceEntity,
                newItem: ProvincePlaceEntity
            ): Boolean = oldItem.areaCode == newItem.areaCode

            override fun areContentsTheSame(
                oldItem: ProvincePlaceEntity,
                newItem: ProvincePlaceEntity
            ): Boolean = oldItem == newItem
        }
    }
}
