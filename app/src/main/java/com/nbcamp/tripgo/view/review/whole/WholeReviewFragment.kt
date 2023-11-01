package com.nbcamp.tripgo.view.review

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentReviewBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.review.detail.ReviewDetailFragment
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel


class WholeReviewFragment : Fragment() {
    private var _binding: FragmentReviewBinding? = null
    private val binding: FragmentReviewBinding
        get() = _binding!!
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var allSchedule: List<ReviewWritingModel>
    private lateinit var categoryTitle: String
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewViewModel: ReviewViewModel by viewModels { ReviewViewModelFactory() }
    private val reviewAdapter: WholeReviewAdapter by lazy {
        WholeReviewAdapter(requireActivity()) { model ->
            goToReviewDetailFragment(model)
        }
    }
    private val bottomSheetAdapter: BottomSheetAdapter by lazy {
        BottomSheetAdapter { category ->
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            reviewViewModel.setFilteredReview(
                category,
                categoryTitle
            )
        }
    }
    private val behavior by lazy {
        BottomSheetBehavior.from(
            binding.commentBottomSheet.root
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViews() = with(binding) {
        reviewRecyclerView.run {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
        reviewChipRegion.setOnClickListener {
            val categoryList = resources.getStringArray(R.array.si_do)
            runBottomSheetForFiltering(reviewChipRegion.text, categoryList)
        }
        reviewChipGender.setOnClickListener {
            val categoryList = resources.getStringArray(R.array.gender)
            runBottomSheetForFiltering(reviewChipGender.text, categoryList)
        }
        reviewChipAge.setOnClickListener {
            val categoryList = resources.getStringArray(R.array.generation)
            runBottomSheetForFiltering(reviewChipAge.text, categoryList)
        }
        reviewChipCompanion.setOnClickListener {
            val categoryList = resources.getStringArray(R.array.companion)
            runBottomSheetForFiltering(reviewChipCompanion.text, categoryList)
        }
        reviewDetailRestore.setOnClickListener {
            reviewViewModel.setFilteredReview("", "")
        }

        reviewViewModel.getAllReviews()
    }

    private fun runBottomSheetForFiltering(
        text: CharSequence?,
        categoryList: Array<String>
    ) = with(binding) {

        // 높이 조절
        val layoutParams = commentBottomSheet.root.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight()
        commentBottomSheet.root.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        categoryTitle = text.toString()
        commentBottomSheet.reviewBottomSheetTitleTextView.text = text
        commentBottomSheet.reviewBottomSheetRecyclerView.run {
            adapter = bottomSheetAdapter
        }
        bottomSheetAdapter.addItems(categoryList.toList())
    }

    private fun initViewModel() = with(reviewViewModel) {
        reviewUiState.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                loadingDialog.run {
                    setVisible()
                    state.message?.let { setText(it) }
                }
            } else {
                loadingDialog.run {
                    setInvisible()
                    state.message?.let { setText(it) }
                }
                if(state.allSchedules?.isEmpty() == true) {
                    binding.reviewDetailNoticeTextView.run {
                        isVisible = true
                        text = "아직 작성 된 리뷰가 없어요.. 첫 리뷰를 작성해보세요 :)"
                    }
                    reviewAdapter.submitList(emptyList())
                    return@observe
                }
            }
            binding.reviewDetailNoticeTextView.isVisible = false
            allSchedule = state.allSchedules ?: emptyList()
            reviewAdapter.submitList(allSchedule)
        }

        filteredList.observe(viewLifecycleOwner) { list ->
            if(list.isEmpty()) {
                binding.reviewDetailNoticeTextView.run {
                    isVisible = true
                    text = "조건에 맞는 리뷰가 존재 하지 않습니다.. :)"
                }
                reviewAdapter.submitList(emptyList())
                return@observe
            }
            binding.reviewDetailNoticeTextView.isVisible = false
            reviewAdapter.submitList(list)
        }
    }

    private fun goToReviewDetailFragment(model: ReviewWritingModel) {
        val transactionReviewWriting = parentFragmentManager.beginTransaction()
        // review Detail fragment로 데이터 전달
        sharedViewModel.setReviewDetailModel(model)
        transactionReviewWriting.replace(
            R.id.main_fragment_container,
            ReviewDetailFragment.newInstance()
        ).addToBackStack(null)
            .commit()
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        return getWindowHeight() * 50 / 100
    }

    private fun getWindowHeight(): Int {
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance() = WholeReviewFragment()

        const val TAG = "REVIEW_FRAGMENT"
    }
}
