package com.nbcamp.tripgo.view.review.whole

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
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ItemReviewMainBinding
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

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
            // 리뷰 리사이클러뷰 아이템 데이터
            reviewItemTitleTextView.text = model.tourTitle
            reviewItemDateTextView.text = model.schedule
            reviewItemRatingBar.rating = model.rating
            reviewItemImageView.load(model.reviewImageUrl)
            reviewItemShortsDescriptionTextView.text = model.reviewText

            // 리뷰 리사이클러뷰 칩 동적 주입
            val siDo = model.address.split(" ").first()
            reviewItemChipGroup.addView(
                Chip(
                    context,
                    null,
                    com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action
                ).apply {
                    when {
                        siDo.isEmpty() -> "#${context.getString(R.string.somewhere)}".also {
                            text = it
                        }

                        siDo.length != 4 -> "#${siDo.take(2)}".also { text = it }
                        siDo.length == 4 -> "#${siDo[0]}${siDo[2]}".also { text = it }
                    }
                    chipBackgroundColor = ColorStateList.valueOf(
                        Color.parseColor(
                            context.getString(
                                R.string.region_background
                            )
                        )
                    )
                    setTextColor(
                        Color.parseColor(
                            context.getString(R.string.region_text)
                        )
                    )
                    chipEndPadding = 8f
                    chipStartPadding = 8f
                }
            )
            tags.forEach { tag ->
                reviewItemChipGroup.addView(
                    Chip(
                        context,
                        null,
                        com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action
                    ).apply {
                        "#$tag".also { text = it }
                        chipBackgroundColor = ColorStateList.valueOf(
                            Color.parseColor(
                                context.getString(
                                    R.string.other_background
                                )
                            )
                        )
                        setTextColor(
                            Color.parseColor(
                                context.getString(R.string.other_text)
                            )
                        )
                        chipEndPadding = 8f
                        chipStartPadding = 8f
                    }
                )
            }
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
