package com.nbcamp.tripgo.view.review.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nbcamp.tripgo.R

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
    private var data: List<ReviewItem> = ArrayList()

    // 데이터 설정 메서드
    fun setData(reviewItems: List<ReviewItem>) {
        data = reviewItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewItem = data[position]
        holder.bind(reviewItem)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: AppCompatTextView = itemView.findViewById(R.id.review_item_title_text_view)
        private val dateTextView: AppCompatTextView = itemView.findViewById(R.id.review_item_date_text_view)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.review_item_rating_bar)
        private val descriptionTextView: AppCompatTextView = itemView.findViewById(R.id.review_item_shorts_description_text_view)
        private val reviewImageView: AppCompatImageView = itemView.findViewById(R.id.review_item_image_view)

        // 데이터 바인딩 메서드
        fun bind(reviewItem: ReviewItem) {
            titleTextView.text = reviewItem.title
            dateTextView.text = reviewItem.date
            ratingBar.rating = reviewItem.rating
            descriptionTextView.text = reviewItem.shortDescription
            reviewImageView.load(reviewItem.reviewImageUrl)
        }
    }
}
