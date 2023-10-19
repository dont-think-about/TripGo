package com.nbcamp.tripgo.view.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.view.login.LogInActivity
import com.nbcamp.tripgo.view.mypage.favorite.FavoriteFragment
import com.nbcamp.tripgo.view.review.ReviewFragment

// MVVM 패턴 적용 해야됨
class MyPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)


        // review_layout 클릭 이벤트 처리
        val reviewLayout = view.findViewById<LinearLayout>(R.id.review_layout)
        reviewLayout.setOnClickListener {
            val transaction_review = parentFragmentManager.beginTransaction()
            transaction_review.replace(R.id.main_fragment_container, ReviewFragment())
            transaction_review.addToBackStack(null)
            transaction_review.commit()
        }

        // mypage_zzim_layout 클릭 이벤트 처리
        val zzimLayout = view.findViewById<LinearLayout>(R.id.mypage_zzim_layout)
        zzimLayout.setOnClickListener {
            val transaction_zzim = parentFragmentManager.beginTransaction()
            transaction_zzim.replace(R.id.main_fragment_container, FavoriteFragment()) // 다른 프래그먼트로 전환
            transaction_zzim.addToBackStack(null)
            transaction_zzim.commit()
        }

        // Logout 클릭 이벤트 처리
        val mypage_logout_button = view.findViewById<Button>(R.id.mypage_logout_button)
        mypage_logout_button.setOnClickListener {
            val intent = Intent(context, LogInActivity::class.java)
            startActivity(intent)
        }

        return view
    }
    companion object {
        fun newInstance() = MyPageFragment()

        const val TAG = "MY_PAGE_FRAGMENT"
    }
}

