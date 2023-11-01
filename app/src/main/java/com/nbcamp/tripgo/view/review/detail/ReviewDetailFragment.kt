package com.nbcamp.tripgo.view.review.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentReviewDetailBinding
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

class ReviewDetailFragment : Fragment() {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding: FragmentReviewDetailBinding
        get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewDetailViewModel: ReviewDetailViewModel by viewModels { ReviewDetailViewModelFactory() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() = with(sharedViewModel) {
        reviewDetailModel.observe(viewLifecycleOwner) {
            initViews(it)
        }
        reviewDetailViewModel.userStatus.observe(viewLifecycleOwner) { model ->
            initUserReviewGrade(model)
        }
    }

    private fun initViews(model: ReviewWritingModel?) = with(binding) {
        if (model?.userImageUrl.isNullOrEmpty() || model?.userImageUrl == "") {
            reviewDetailUserImageView.load(R.drawable.icon_user)
        } else {
            reviewDetailUserImageView.load(model?.userImageUrl)
        }
        reviewDetailUserName.text = model?.userNickName
        reviewDetailImageView.load(model?.reviewImageUrl)
        reviewDetailTitleTextView.text = model?.tourTitle
        reviewDetailAddressTextView.text =
            if (model?.address.isNullOrEmpty()) "주소 정보가 없습니다" else model?.address
        reviewDetailFestivalDateTextView.text = model?.schedule
        reviewDetailDescriptionTextView.text = model?.reviewText
        reviewDetailRatingBar.rating = model?.rating ?: 0f
        reviewDetailViewModel.getUserStatus(model?.userNickName)
        // 홈 버튼
        reviewDetailButtonHome.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    MainActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        }
        // 공유 버튼
        reviewDetailButtonShare.setOnClickListener {
            sharingPlace(model)
        }
    }

    // 리뷰 갯수에 따라 뱃지 다르게 보여주기
    private fun initUserReviewGrade(model: UserStatus?) = with(binding) {
        if (model == null) {
            return@with
        }
        when (model.reviewCount) {
            in 0..5 -> reviewDetailMedal.load(R.drawable.icon_third_place)
            in 6..10 -> reviewDetailMedal.load(R.drawable.icon_second_place)
            in 11..20 -> reviewDetailMedal.load(R.drawable.icon_first_place)
            else -> reviewDetailMedal.load(R.drawable.icon_thropy)
        }
    }


    private fun sharingPlace(model: ReviewWritingModel?) {
        val placeInfo = "${model?.tourTitle}\n${model?.reviewText}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, placeInfo)
            type = "text/plain"
        }
        val sharingIntent = Intent.createChooser(intent, "공유하기")
        startActivity(sharingIntent)
    }

    companion object {
        fun newInstance() = ReviewDetailFragment()
    }
}
