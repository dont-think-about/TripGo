package com.nbcamp.tripgo.view.review

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot // 추가: QuerySnapshot import
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.App


class ReviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review, container, false)

        // Firebase 데이터 불러오는 부분
        suchdata(view) // suchdata 함수를 onCreateView 내에서 호출

        return view
    }

    companion object {
        fun newInstance() = ReviewFragment()

        const val TAG = "REVIEW_FRAGMENT"
    }

    // suchdata 함수는 Fragment 클래스 내에 멤버 함수로 정의
    private fun suchdata(view: View) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email
        var realstoredb: CollectionReference? = null
        val firestoredb = FirebaseFirestore.getInstance()
        val kakaouser = App.kakaoUser?.email
        if (kakaouser == null) {
            realstoredb = firestoredb.collection("reviews").document(userEmail.toString()).collection("review")
            Log.d(ReviewFragment.TAG, "구글 계정 연결 완료(Review)")
        } else if (kakaouser != null) {
            realstoredb = firestoredb.collection("reviews").document(kakaouser.toString()).collection("review")
            Log.d(ReviewFragment.TAG, "카카오 계정 연결 완료 (Review)")
        } else {
            Log.d(ReviewFragment.TAG, "둘다 로그인 불가")
        }

        realstoredb?.get()?.addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val reviewItems = ArrayList<ReviewItem>()
                for (document in querySnapshot) {
                    val reviewdata = document.data
                    val reviewtitle = reviewdata["tourTitle"] as? String ?: ""
                    val reviewdate = reviewdata["schedule"] as? String ?: ""
                    val reviewrating = (reviewdata["rating"] as? Double)?.toFloat() ?: 0.0f
                    val reviewdescription = reviewdata["/* 물어보기*/"] as? String ?: "" // 리뷰쓴내용

                    val reviewItem = ReviewItem(reviewtitle, reviewdate, reviewrating, reviewdescription)
                    reviewItems.add(reviewItem)
                }

                val recyclerView = view.findViewById<RecyclerView>(R.id.review_recycler_view)
                val reviewAdapter = ReviewAdapter()
                reviewAdapter.setData(reviewItems)

                recyclerView.adapter = reviewAdapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
}

