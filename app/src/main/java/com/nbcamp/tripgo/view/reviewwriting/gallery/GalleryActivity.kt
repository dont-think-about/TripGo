package com.nbcamp.tripgo.view.reviewwriting.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.nbcamp.tripgo.databinding.ActivityGalleryBinding
import com.nbcamp.tripgo.util.extension.ContextExtension.toast

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding

    // 리사이클러뷰의 아이템을 클릭했을 떄, 사진을 선택함
    private val galleryAdapter by lazy {
        GalleryPhotoListAdapter {
            galleryViewModel.selectPhoto(it)
        }
    }

    private val galleryViewModel: GalleryViewModel by viewModels { GalleryViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initViewModel()
    }

    private fun initViews() = with(binding) {
        galleryRecyclerView.adapter = galleryAdapter
        galleryViewModel.fetchPhotos()
    }

    private fun initViewModel() = with(galleryViewModel) {
        galleryState.observe(this@GalleryActivity) { state ->
            with(binding) {
                when (state) {
                    is GalleryUiState.Loading -> {
                        galleryProgressBar.isVisible = true
                        galleryRecyclerView.isGone = true
                    }

                    is GalleryUiState.Success -> {
                        galleryProgressBar.isGone = true
                        galleryRecyclerView.isVisible = true
                        galleryAdapter.submitList(state.photoList.toMutableList())
                    }

                    is GalleryUiState.Error -> {
                        finish()
                        toast(state.message)
                    }

                    is GalleryUiState.PickPhoto -> {
                        setResult(RESULT_OK, Intent().apply {
                            putExtra(URI_LIST_KEY, state.photo)
                        })
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        fun newIntent(activity: Activity) = Intent(activity, GalleryActivity::class.java)
        const val URI_LIST_KEY = "uriList"
    }
}
