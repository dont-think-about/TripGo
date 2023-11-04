package com.nbcamp.tripgo.view.home.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity
import com.nbcamp.tripgo.databinding.ItemMainTourCardBinding
import com.nbcamp.tripgo.view.App

class NearbyPlaceAdapter(
    private val context: Context,
    private val onClickItem: (String) -> Unit
) : RecyclerView.Adapter<NearbyPlaceAdapter.NearbyPlaceViewHolder>() {

    private val list = arrayListOf<NearbyPlaceEntity>()

    fun setList(items: List<NearbyPlaceEntity>?) {
        if (items == null) {
            return
        }
        list.addAll(items)
        notifyItemRangeChanged(list.size - 10, list.size)
    }

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

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: NearbyPlaceViewHolder, position: Int) {
        holder.bind(list[position])
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

            // 이미지가 없을 떄 텍스트 색 바꾸기
            when {
                model.imageUrl.isEmpty() -> {
                    itemMainImageView.scaleType = ImageView.ScaleType.CENTER
                    itemMainImageView.load(R.drawable.icon_no_image)
                    itemTitleTextView.setTextColor(Color.BLACK)
                    itemDescriptionTextView.setTextColor(Color.BLACK)
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
            itemView.setOnClickListener {
                onClickItem(model.contentId)
            }
        }
    }
}
