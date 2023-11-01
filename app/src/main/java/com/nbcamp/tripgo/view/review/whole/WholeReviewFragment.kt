package com.nbcamp.tripgo.view.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewViewModel: ReviewViewModel by viewModels { ReviewViewModelFactory() }
    private val reviewAdapter: WholeReviewAdapter by lazy {
        WholeReviewAdapter(requireActivity()) { model ->
            goToReviewDetailFragment(model)
        }
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
        reviewViewModel.getAllReviews()
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
            }
            allSchedule = state.allSchedules ?: emptyList()
            reviewAdapter.submitList(allSchedule)
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
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance() = WholeReviewFragment()

        const val TAG = "WHOLE_REVIEW_FRAGMENT"
    }
}
