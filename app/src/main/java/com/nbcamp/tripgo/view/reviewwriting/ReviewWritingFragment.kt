package com.nbcamp.tripgo.view.reviewwriting

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.GalleryPhotoEntity
import com.nbcamp.tripgo.databinding.FragmentReviewWritingBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.calendar.WritingType
import com.nbcamp.tripgo.view.main.MainViewModel
import com.nbcamp.tripgo.view.reviewwriting.gallery.GalleryActivity
import com.nbcamp.tripgo.view.reviewwriting.gallery.GalleryActivity.Companion.URI_LIST_KEY

class ReviewWritingFragment : Fragment() {

    private var _binding: FragmentReviewWritingBinding? = null
    private val binding: FragmentReviewWritingBinding
        get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels()
    private val reviewWritingViewModel: ReviewWritingViewModel by viewModels { ReviewWritingViewModelFactory() }
    private lateinit var calendarUserEntity: CalendarUserModel
    private lateinit var gender: String
    private lateinit var generation: String
    private lateinit var companion: String
    private lateinit var imageUrl: String
    private lateinit var loadingDialog: LoadingDialog
    private var reviewText = ""
    private var rating: Float = 0f
    private var writingType = WritingType.NEW

    // 갤러리 액티비티를 실행 하는 런처
    private val getGalleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val entity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra(URI_LIST_KEY, GalleryPhotoEntity::class.java)
                } else {
                    it.data?.getParcelableExtra(URI_LIST_KEY)
                }
                if (entity != null) {
                    binding.reviewWritingAddImageView.isGone = true
                    imageUrl = entity.uri.toString()
                    binding.reviewWritingImageButton.load(entity.uri)
                } else {
                    requireActivity().toast(getString(R.string.cant_get_image_to_gallery))
                }
            } else {
                requireActivity().toast(getString(R.string.cant_get_image_to_gallery))
                return@registerForActivityResult
            }
        }

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
        initViewModel()
        initSharedViewModel()
    }

    private fun initViews() = with(binding) {
        loadingDialog = LoadingDialog(requireActivity())
        reviewWritingButtonBack.setOnClickListener {
            clickBackButton()
        }

        reviewWritingCancelButton.setOnClickListener {
            clickBackButton()
        }

        // TODO 드롭다운(스피너로 변경?)
        reviewWritingGenderButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickGenderGroupEvent(checkedId)
        }

        reviewWritingAgeButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickAgeGroupEvent(checkedId)
        }

        reviewWritingCompanionButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)
                return@addOnButtonCheckedListener
            reviewWritingViewModel.onClickCompanionGroupEvent(checkedId)
        }

        reviewWritingTextInputEditText.doOnTextChanged { text, _, _, _ ->
            reviewWritingViewModel.editReviewWriting(text)
        }

        reviewWritingImageButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sharedViewModel.getGalleryPermissionEvent(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                sharedViewModel.getGalleryPermissionEvent(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        reviewWritingRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            reviewWritingViewModel.setRatingReview(rating)
        }

        reviewWritingSubmitButton.setOnClickListener {
            if (::gender.isInitialized.not()
                || ::generation.isInitialized.not()
                || ::companion.isInitialized.not()
                || ::imageUrl.isInitialized.not()
                || imageUrl.isEmpty()
                || reviewText.isEmpty()
            ) {
                requireActivity().toast("모든 항목을 입력해주세요")
                return@setOnClickListener
            }

            val reviewWritingModel = ReviewWritingModel(
                gender = gender,
                generation = generation,
                companion = companion,
                reviewText = reviewText,
                imageUrl = imageUrl,
                rating = rating
            )
            reviewWritingViewModel.saveReview(reviewWritingModel, calendarUserEntity, writingType)
        }
    }

    private fun initSharedViewModel() = with(sharedViewModel) {
        calendarToReviewModel.observe(viewLifecycleOwner) { model ->
            calendarUserEntity = model
            if (model.writingType == WritingType.MODIFY) {
                reviewWritingViewModel.modifyReviewWriting(model)
                writingType = WritingType.MODIFY
            }
        }
        eventRunGallery.observe(viewLifecycleOwner) {
            navigatePhotos()
        }
    }

    private fun initViewModel() = with(reviewWritingViewModel) {
        eventButtonClick.observe(viewLifecycleOwner) { event ->
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

                is ReviewWritingEvent.EventSubmitReview -> {
                    invokeEventSubmitReview(event)
                }

                else -> Unit
            }
        }
        eventLoadingReview.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ReviewWritingEvent.EventLoadingPastReviewDetail -> {
                    invokeLoadingPastReviewDetail(event)
                }

                else -> Unit
            }
        }
    }

    private fun invokeLoadingPastReviewDetail(event: ReviewWritingEvent.EventLoadingPastReviewDetail) {
        with(binding) {
            loadingDialog.setText(event.message)
            when (event.isLoading) {
                true -> {
                    loadingDialog.setVisible()
                }

                false -> {
                    loadingDialog.setInvisible()
                    if (event.pastModel != null) {
                        reviewText = event.pastModel.reviewText
                        imageUrl = ""
                        rating = event.pastModel.rating
                        reviewWritingTextInputEditText.setText(event.pastModel.reviewText)
                        reviewWritingRatingBar.rating = event.pastModel.rating
                        // TODO 버튼 레이아웃 변경 회의 후 수정하기
                        return
                    }
                }
            }
        }
    }

    private fun invokeEventSubmitReview(event: ReviewWritingEvent.EventSubmitReview) {
        loadingDialog.setText(event.message)
        when (event.isLoading) {
            // 저장 로딩
            true -> loadingDialog.setVisible()
            // 성공 or 실패
            false -> {
                requireActivity().toast(event.message)
                loadingDialog.setInvisible()
                if (event.message.contains("실패").not()) {
                    clickBackButton()
                    return
                }
            }
        }
    }

    private fun navigatePhotos() {
        getGalleryImageLauncher.launch(GalleryActivity.newIntent(requireActivity()))
    }

    private fun clickBackButton() {
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        sharedViewModel.onClickBackButton()
    }

    companion object {
        fun newInstance() = ReviewWritingFragment()
    }
}
