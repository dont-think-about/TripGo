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
    private lateinit var genderValue: String
    private lateinit var generationValue: String
    private lateinit var companionValue: String
    private lateinit var imageUrlValue: String
    private lateinit var loadingDialog: LoadingDialog
    private var reviewTextValue = ""
    private var ratingValue: Float = 0f
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
                    imageUrlValue = entity.uri.toString()
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

        // material toggle button group -> chip group 으로 변경
        reviewWritingGenderButtonGroup.setOnCheckedStateChangeListener { group, _ ->
            val selectedChip = group.checkedChipId
            reviewWritingViewModel.onClickGenderGroupEvent(selectedChip)
        }

        reviewWritingAgeButtonGroup.setOnCheckedStateChangeListener { group, _ ->
            val selectedChip = group.checkedChipId
            reviewWritingViewModel.onClickAgeGroupEvent(selectedChip)
        }

        reviewWritingCompanionButtonGroup.setOnCheckedStateChangeListener { group, _ ->
            val selectedChip = group.checkedChipId
            reviewWritingViewModel.onClickCompanionGroupEvent(selectedChip)
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
            if (::genderValue.isInitialized.not() ||
                ::generationValue.isInitialized.not() ||
                ::companionValue.isInitialized.not() ||
                ::imageUrlValue.isInitialized.not() ||
                imageUrlValue.isEmpty() ||
                reviewTextValue.isEmpty()
            ) {
                requireActivity().toast("모든 항목을 입력해주세요")
                return@setOnClickListener
            }

            val reviewWritingModel = calendarUserEntity.model?.contentId?.let { contentId ->
                ReviewWritingModel(
                    contentId = contentId,
                    gender = genderValue,
                    generation = generationValue,
                    companion = companionValue,
                    reviewText = reviewTextValue,
                    imageUrl = imageUrlValue,
                    rating = ratingValue
                )
            }
            if (reviewWritingModel != null)
                reviewWritingViewModel.saveReview(
                    reviewWritingModel,
                    calendarUserEntity,
                    writingType
                )
        }
    }

    private fun initSharedViewModel() = with(sharedViewModel) {
        calendarToReviewModel.observe(viewLifecycleOwner) { model ->
            calendarUserEntity = model
            "${model.model?.title}${getString(R.string.how_feel)}"
                .also { binding.reviewNoticeTextView.text = it }
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
                    companionValue = event.companion
                }

                is ReviewWritingEvent.EventGenderClick -> {
                    genderValue = event.gender
                }

                is ReviewWritingEvent.EventGenerationClick -> {
                    generationValue = event.generation
                }

                is ReviewWritingEvent.EventReviewWriting -> {
                    reviewTextValue = event.reviewText
                    if (reviewTextValue.length >= 100) {
                        requireActivity().toast(getString(R.string.please_write_review_under_100))
                    }
                }

                is ReviewWritingEvent.EventSetRating -> {
                    ratingValue = event.rating
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
                    val model = event.pastModel
                    if (model != null) {
                        model.run {
                            reviewTextValue = reviewText
                            imageUrl = ""
                            ratingValue = rating
                            genderValue = gender
                            generationValue = generation
                            companionValue = companion
                            reviewWritingTextInputEditText.setText(event.pastModel.reviewText)
                            reviewWritingRatingBar.rating = event.pastModel.rating
                            chipSet(event.pastModel) // 기존 리뷰의 칩 값 세팅
                        }
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

    private fun chipSet(pastModel: ReviewWritingModel) = with(binding) {
        when (pastModel.gender) {
            getString(R.string.man) -> reviewWritingToggleManButton.isChecked = true
            getString(R.string.woman) -> reviewWritingToggleWomanButton.isChecked = true
        }
        when (pastModel.generation) {
            getString(R.string.generation_10) -> reviewWritingToggle10sButton.isChecked = true
            getString(R.string.generation_20) -> reviewWritingToggle20sButton.isChecked = true
            getString(R.string.generation_30) -> reviewWritingToggle30sButton.isChecked = true
            getString(R.string.generation_40) -> reviewWritingToggle40sButton.isChecked = true
            getString(R.string.generation_50_over) -> reviewWritingToggle50sOverButton.isChecked =
                true
        }
        when (pastModel.companion) {
            getString(R.string.family) -> reviewWritingToggleFamilyButton.isChecked = true
            getString(R.string.bf_gf) -> reviewWritingToggleBfGfButton.isChecked = true
            getString(R.string.friends) -> reviewWritingToggleFriendsButton.isChecked = true
            getString(R.string.solo) -> reviewWritingToggleSoloButton.isChecked = true
            getString(R.string.pet) -> reviewWritingTogglePetButton.isChecked = true
        }
    }

    companion object {
        fun newInstance() = ReviewWritingFragment()
    }
}
