package com.nbcamp.tripgo.view.attraction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.model.area.AreaItem
import com.nbcamp.tripgo.databinding.AttractionRecyclerviewItemBinding

class AttractionsAdapter(private val onClickItem: (AreaItem) -> Unit) :
    ListAdapter<AreaItem, AttractionsAdapter.AttractionViewHolder>(AttractionDiffCallback) {

    private var userLat = 0.0 // 사용자 위도
    private var userLon = 0.0 // 사용자 경도

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        notifyDataSetChanged()
    } // 위치 정보 변경 후 , RecyclerView 갱신

    private fun calculateDistanceTo(
        mapx: Double,
        mapy: Double,
        userLat: Double,
        userLon: Double
    ): Double {
        val R = 6371.0

        val dLat = Math.toRadians(mapy - userLat)
        val dLon = Math.toRadians(mapx - userLon)

        val a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) *
                Math.cos(Math.toRadians(mapy)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    } // 사용자 위치와 주어진 좌표 간의 거리를 계산 하는 함수

    fun attractionDistance(recyclerView: RecyclerView) {
        val sortedList = currentList.sortedBy { item ->
            calculateDistanceTo(item.mapx.toDouble(), item.mapy.toDouble(), userLat, userLon)
        }
        submitList(sortedList.toList()) {
            recyclerView.scrollToPosition(0)
        }
    } // Tour 아이템 거리순 으로 정렬

    fun attractionDate(recyclerView: RecyclerView) {
        val sortedByDate = currentList.sortedBy { it.createdtime }
        submitList(sortedByDate) {
            recyclerView.scrollToPosition(0)
        }
    } // Tour 아이템 날짜순 으로 정렬

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val binding = AttractionRecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttractionViewHolder(binding, onClickItem, ::calculateDistanceTo)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, userLat, userLon)
    }

    class AttractionViewHolder(
        private val binding: AttractionRecyclerviewItemBinding,
        private val onClickItem: (AreaItem) -> Unit,
        private val calculateDistance: (Double, Double, Double, Double) -> Double
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AreaItem, userLat: Double, userLon: Double) {
            itemView.setOnClickListener {
                onClickItem(item)
            }
            with(binding) {
                attractionTitle.text = item.title
                attractionAddress.text = item.addr1
                attractionPosition.text = binding.root.context.getString(
                    R.string.distance_from_my_location,
                    calculateDistance(
                        item.mapx.toDouble(),
                        item.mapy.toDouble(),
                        userLat,
                        userLon
                    ).toInt()
                )
                myImage.load(item.firstimage)
            }
        }
    }

    companion object AttractionDiffCallback : DiffUtil.ItemCallback<AreaItem>() {
        override fun areItemsTheSame(oldItem: AreaItem, newItem: AreaItem): Boolean {
            return oldItem.contentid == newItem.contentid // contentid를 고유 식별자로 사용하여 비교
        }

        override fun areContentsTheSame(oldItem: AreaItem, newItem: AreaItem): Boolean {
            return oldItem == newItem
        }
    }
}
