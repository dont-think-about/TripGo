package com.nbcamp.tripgo.view.review.whole

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.databinding.ItemReviewBottomSheetSelectorBinding

class BottomSheetAdapter(
    private val onClickItem: (String) -> Unit
) : RecyclerView.Adapter<BottomSheetAdapter.BottomSheetViewHolder>() {
    private val categoryList = arrayListOf<String>()

    fun addItems(strings: Collection<String>) {
        categoryList.clear()
        categoryList.addAll(strings)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetViewHolder {
        return BottomSheetViewHolder(
            ItemReviewBottomSheetSelectorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClickItem
        )
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: BottomSheetViewHolder, position: Int) {
        holder.bind(categoryList[position])
    }

    inner class BottomSheetViewHolder(
        private val binding: ItemReviewBottomSheetSelectorBinding,
        private val onClickItem: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(str: String) = with(binding) {
            itemReviewBottomSheet.run {
                text = str
                setOnClickListener {
                    onClickItem(str)
                }
            }
        }
    }
}
