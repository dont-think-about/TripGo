package com.nbcamp.tripgo.view.reviewwriting

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.FragmentReviewWritingBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.main.MainViewModel

class ReviewWritingFragment : Fragment() {

    private var _binding: FragmentReviewWritingBinding? = null
    private val binding: FragmentReviewWritingBinding
        get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewWritingViewModel: ReviewWritingViewModel by viewModels { ReviewWritingViewModelFactory() }
    private lateinit var calendarUserEntity: CalendarUserEntity
    private lateinit var gender: String
    private lateinit var generation: String
    private lateinit var companion: String
    private var reviewText = ""
    private var rating: Float = 0f
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewWritingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initSharedViewModel()
        initViewModel()
    }

    private fun initViews() = with(binding) {
        reviewWritingButtonBack.setOnClickListener {
            clickBackButton()
        }

        reviewWritingCancelButton.setOnClickListener {
            clickBackButton()
        }

        reviewWritingGenderButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickGenderGroupEvent(checkedId)
        }

        reviewWritingAgeButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickAgeGroupEvent(checkedId)
        }

        reviewWritingCompanionButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickCompanionGroupEvent(checkedId)
        }

        reviewWritingTextInputEditText.doOnTextChanged { text, start, before, count ->
            reviewWritingViewModel.editReviewWriting(text)
        }

        reviewWritingImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sharedViewModel.getGalleryPermissionEvent(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                sharedViewModel.getGalleryPermissionEvent(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
//            checkGalleryPermissions()
        }

        reviewWritingRatingBar.setOnRatingBarChangeListener { ratingBar, rating, isChecked ->
            reviewWritingViewModel.setRatingReview(rating)
        }

        reviewWritingSubmitButton.setOnClickListener {
            if (::gender.isInitialized.not() || ::generation.isInitialized.not() || ::companion.isInitialized.not() || reviewText.isEmpty()) {
                requireActivity().toast("모든 항목을 입력해주세요")
                return@setOnClickListener
            }
        }
    }

    private fun initSharedViewModel() = with(sharedViewModel) {
        calendarToReviewModel.observe(viewLifecycleOwner) { model ->
            calendarUserEntity = model
        }
    }

    private fun initViewModel() = with(reviewWritingViewModel) {
        eventButtonClick.observe(viewLifecycleOwner) { event ->
            Log.e("", event.toString())
            when (event) {
                is ReviewWritingEvent.EventCompanionClick -> {
                    companion = event.companion
                }

                is ReviewWritingEvent.EventGenderClick -> {
                    gender = event.gender
                }

                is ReviewWritingEvent.EventGenerationClick -> {
                    generation = event.generation
                }

                is ReviewWritingEvent.EventReviewWriting -> {
                    reviewText = event.reviewText
                    if (reviewText.length >= 100) {
                        requireActivity().toast(getString(R.string.please_write_review_under_100))
                    }
                }

                is ReviewWritingEvent.EventSetRating -> {
                    rating = event.rating
                }
            }
        }
    }

    private fun clickBackButton() {
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        sharedViewModel.onClickBackButton()
    }

    companion object {
        fun newInstance() = ReviewWritingFragment()
    }
}
