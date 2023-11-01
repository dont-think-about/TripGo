package com.nbcamp.tripgo.view.review.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.nbcamp.tripgo.databinding.FragmentReviewDetailBinding
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel

class ReviewDetailFragment : Fragment() {

    private var _binding: FragmentReviewDetailBinding? = null
    private val binding: FragmentReviewDetailBinding
        get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels()
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
    }

    private fun initViews(model: ReviewWritingModel?) {
        println(model)
    }

    companion object {
        fun newInstance() = ReviewDetailFragment()
    }
}
