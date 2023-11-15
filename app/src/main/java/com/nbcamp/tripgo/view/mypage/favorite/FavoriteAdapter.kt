package com.nbcamp.tripgo.view.mypage.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App

class FavoriteAdapter : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {
    private var favorite_data: List<FavoriteItem> = emptyList() // 초기화된 빈 리스트 사용

    fun setData(favoriteItems: List<FavoriteItem>) {
        favorite_data = favoriteItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteItem = favorite_data[position]
        holder.bind(favoriteItem)
    }

    override fun getItemCount(): Int {
        return favorite_data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favorite_title: AppCompatTextView = itemView.findViewById(R.id.favorite_item_region_name_textview)
        private val favorite_imageurl: AppCompatImageView = itemView.findViewById(R.id.favorite_item_imageview)
        private val favorite_description: AppCompatTextView = itemView.findViewById(R.id.favorite_item_region_explantion_textivew)
        private val favorite_address: AppCompatTextView = itemView.findViewById(R.id.favorite_item_region_distance_textview)
        private val favorite_likebt: AppCompatImageView = itemView.findViewById(R.id.favorite_item_zzim_imageview)

        fun bind(favoriteItem: FavoriteItem) {
            favorite_title.text = favoriteItem.title
            favorite_description.text = favoriteItem.shortDescription
            favorite_address.text = favoriteItem.address
            favorite_imageurl.load(favoriteItem.imageUrl)

            val isFavoriteSelected = favoriteItem.isSelected

            val favoriteImageResource = if (isFavoriteSelected) {
                R.drawable.icon_empty_favorite
            } else {
                R.drawable.icon_favorite
            }
            favorite_likebt.setImageResource(favoriteImageResource)

            favorite_likebt.setOnClickListener {
                val firestore = FirebaseFirestore.getInstance()
                val collectionPath = favoriteItem.collectionPath

                if (collectionPath != null) {
                    val deletePath = firestore.document(collectionPath)
                    deletePath.delete()
                        .addOnSuccessListener {
                            val position = favorite_data.indexOf(favoriteItem)
                            if (position != -1) {
                                favorite_data = favorite_data.toMutableList().apply {
                                    remove(favoriteItem)
                                }
                                notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }

                else {
                }
            }
        }
    }

    // 사용자 이메일을 검색하고 반환하는 함수
    private fun findUserEmail(): DocumentReference? {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        val storedb = FirebaseFirestore.getInstance()
        val kakaouser = App.kakaoUser?.email

        val userId = kakaouser ?: userEmail
        return userId?.let { storedb.collection("liked").document(it) }
    }
}
