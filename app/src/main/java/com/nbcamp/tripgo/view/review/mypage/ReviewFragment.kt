package com.nbcamp.tripgo.view.review.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App

class ReviewFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val fireStoreDb = FirebaseFirestore.getInstance()
    private val kakaoUser = App.kakaoUser?.email
    private val reviewItems = ArrayList<ReviewItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.review_recycler_view)
        val reviewAdapter = ReviewAdapter()
        recyclerView.adapter = reviewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchReviewData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ChipGroup>(R.id.horizontal_chip_group).isVisible = false
        view.findViewById<ImageView>(R.id.review_detail_restore).isVisible = false
        view.findViewById<TextView>(R.id.review_title_text_view).text = "내 후기"
    }

    private fun fetchReviewData() {
        val userEmail = auth.currentUser?.email
        var reviewCollectionRef = userEmail?.let {
            fireStoreDb.collection("reviews").document(it).collection("review")
        }
        if (kakaoUser != null) {
            reviewCollectionRef =
                fireStoreDb.collection("reviews").document(kakaoUser).collection("review")
        }

        reviewCollectionRef?.get()?.addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                querySnapshot.forEach { document ->
                    val reviewData = document.data
                    val reviewTitle = reviewData["tourTitle"] as? String ?: ""
                    val reviewDate = reviewData["schedule"] as? String ?: ""
                    val reviewWriting = (reviewData["rating"] as? Double)?.toFloat() ?: 0.0f
                    val reviewText = reviewData["reviewText"] as? String ?: ""
                    val reviewImageUrl = reviewData["reviewImageUrl"] as String
                    val reviewItem = ReviewItem(
                        reviewTitle,
                        reviewDate,
                        reviewWriting,
                        reviewText,
                        reviewImageUrl
                    )
                    reviewItems.add(reviewItem)
                }
                (view?.findViewById<RecyclerView>(R.id.review_recycler_view)?.adapter as? ReviewAdapter)?.setData(
                    reviewItems
                )
            }
        }
    }

    companion object {
        fun newInstance() = ReviewFragment()

        const val TAG = "REVIEW_FRAGMENT"
    }
}
