package com.nbcamp.tripgo.view.review

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.chip.Chip
import com.nbcamp.tripgo.databinding.ItemReviewMainBinding
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel
import kotlin.random.Random
import kotlin.random.nextInt

class WholeReviewAdapter(
    private val context: Context,
    private val onClickItem: (ReviewWritingModel) -> Unit
) : ListAdapter<ReviewWritingModel, WholeReviewAdapter.ReviewViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return ReviewViewHolder(
            ItemReviewMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClickItem
        )
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(
        private val binding: ItemReviewMainBinding,
        private val onClickItem: (ReviewWritingModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val tags = mutableSetOf<String>()

        fun bind(model: ReviewWritingModel) = with(binding) {
            reviewItemChipGroup.removeAllViews()
            tags.apply {
                clear()
                if (tags.size <= 2) {
                    add(model.companion)
                    add(model.generation)
                }
            }
            itemView.setOnClickListener {
                onClickItem(model)
            }
            reviewItemTitleTextView.text = model.tourTitle
            reviewItemDateTextView.text = model.schedule
            reviewItemRatingBar.rating = model.rating
            reviewItemImageView.load(model.reviewImageUrl)

            val siDo = model.address.split(" ").first()
            reviewItemChipGroup.addView(
                Chip(context).apply {
                    when {
                        siDo.isEmpty() -> "#어딘가".also { text = it }
                        siDo.length != 4 -> "#${siDo.take(2)}".also { text = it }
                        siDo.length == 4 -> "#${siDo[0]}${siDo[2]}".also { text = it }
                    }
                    chipBackgroundColor = ColorStateList.valueOf(getRandomColor())
                }
            )
            tags.forEach { tag ->
                val red = Random.nextInt(0..255)
                val green = Random.nextInt(0..255)
                val blue = Random.nextInt(0..255)
                val color = Color.argb(10f, red.toFloat(), green.toFloat(), blue.toFloat())
                reviewItemChipGroup.addView(
                    Chip(context).apply {
                        "#${tag}".also { text = it }
                        chipBackgroundColor = ColorStateList.valueOf(color)
                    }
                )
            }
        }

        private fun getRandomColor(): Int {
            val red = Random.nextInt(0..255)
            val green = Random.nextInt(0..255)
            val blue = Random.nextInt(0..255)
            return Color.argb(10f, red.toFloat(), green.toFloat(), blue.toFloat())
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ReviewWritingModel>() {
            override fun areItemsTheSame(
                oldItem: ReviewWritingModel,
                newItem: ReviewWritingModel
            ): Boolean = oldItem.contentId == newItem.contentId

            override fun areContentsTheSame(
                oldItem: ReviewWritingModel,
                newItem: ReviewWritingModel
            ): Boolean = oldItem == newItem
        }
    }
}
