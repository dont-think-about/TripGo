package com.nbcamp.tripgo.view.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App

class ReviewFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val firestoredb = FirebaseFirestore.getInstance()
    private val kakaouser = App.kakaoUser?.email
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

    private fun fetchReviewData() {
        val userEmail = auth.currentUser?.email
        var reviewCollectionRef = userEmail?.let {
            firestoredb.collection("reviews").document(it).collection("review")
        }
        if (kakaouser != null) {
            reviewCollectionRef = firestoredb.collection("reviews").document(kakaouser).collection("review")
        }

        reviewCollectionRef?.get()?.addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                querySnapshot.forEach { document ->
                    val reviewdata = document.data
                    val reviewtitle = reviewdata["tourTitle"] as? String ?: ""
                    val reviewdate = reviewdata["schedule"] as? String ?: ""
                    val reviewrating = (reviewdata["rating"] as? Double)?.toFloat() ?: 0.0f
                    val reviewtext = reviewdata["reviewText"] as? String ?: ""
                    val reviewItem = ReviewItem(reviewtitle, reviewdate, reviewrating, reviewtext)
                    reviewItems.add(reviewItem)
                }
                (view?.findViewById<RecyclerView>(R.id.review_recycler_view)?.adapter as? ReviewAdapter)?.setData(reviewItems)
            }
        }
    }

    companion object {
        fun newInstance() = ReviewFragment()

        const val TAG = "REVIEW_FRAGMENT"
    }

}
