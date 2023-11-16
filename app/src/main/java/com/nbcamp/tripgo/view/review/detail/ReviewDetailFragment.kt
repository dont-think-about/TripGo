package com.nbcamp.tripgo.view.review.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentReviewDetailBinding
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.main.MainActivity
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

class ReviewDetailFragment : Fragment() {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding: FragmentReviewDetailBinding
        get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewDetailViewModel: ReviewDetailViewModel by viewModels { ReviewDetailViewModelFactory() }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            parentFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewDetailBinding.inflate(layoutInflater)
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
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
            initUser(model)
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
            if (model?.address.isNullOrEmpty()) getString(R.string.no_address_info) else model?.address
        reviewDetailFestivalDateTextView.text = model?.schedule
        reviewDetailDescriptionTextView.text = model?.reviewText
        reviewDetailRatingBar.rating = model?.rating ?: 0f
        reviewDetailViewModel.getUserStatus(
            if (App.kakaoUser == null)
                App.firebaseUser?.email
            else
                App.kakaoUser?.email
        )

        // 칩그룹 세팅
        "#${model?.generation}".also { generationChip.text = it }
        "#${model?.gender}".also { genderChip.text = it }
        "#${model?.companion}과 함께".also { companionChip.text = it }

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

        // 뒤로가기 버튼
        reviewDetailButtonBack.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    // 리뷰 갯수에 따라 뱃지 다르게 보여주기
    private fun initUser(model: UserStatus?) = with(binding) {
        if (model == null) {
            return@with
        }
        reviewDetailMedal.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.badge_of_review_count))
                .setView(R.layout.dialog_review_grade)
                .create()
                .show()
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
