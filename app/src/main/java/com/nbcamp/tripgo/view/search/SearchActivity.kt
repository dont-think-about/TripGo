package com.nbcamp.tripgo.view.search

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySearchBinding
import com.nbcamp.tripgo.util.LoadingDialog
import com.nbcamp.tripgo.util.extension.ContextExtension.toast
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var binding: ActivitySearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private val searchViewModel: SearchViewModel by viewModels { SearchViewModelFactory() }
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this)
        setContentView(binding.root)

        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.searchViewpager.adapter = mViewPagerAdapter
        binding.searchTabLayout.setupWithViewPager(binding.searchViewpager)
        adapter = SearchAdapter(this)
        progressBar = findViewById(R.id.progressBar)

        searchViewModel.initAdapter(adapter)

        recyclerView = binding.searchRankRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        bottomSheetLayout = findViewById(R.id.persistent_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // 초기에는 숨긴 상태로 시작

        // RecyclerView 어댑터에 데이터 추가
        searchViewModel.fetchViewPagerData()
        progressBar.visibility = View.VISIBLE

        adapter.clearItem()

        val searchBackImageView = findViewById<ImageView>(R.id.search_back)
        searchBackImageView.setOnClickListener {
            finish()
        }
        searchViewModel.rankList.observe(this) { pullRankList ->
            adapter.additem(pullRankList)

            progressBar.visibility = View.GONE
        }

        searchViewModel.pullData.observe(this) { pullDatalist ->
            if (pullDatalist.isEmpty()) {
                loadingDialog.setVisible()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (loadingDialog.isShowing) {
                        toast("검색 결과가 없습니다.")
                        loadingDialog.setInvisible()
                        return@postDelayed
                    }
                }, 5000)
            } else {
                loadingDialog.setInvisible()
            }
            progressBar.visibility = View.GONE
            val totalLatitude = pullDatalist.map { it.latitude.toDouble() }.sum()
            val totalLongitude = pullDatalist.map { it.longitude.toDouble() }.sum()
            val centerLatitude = totalLatitude / pullDatalist.size
            val centerLongitude = totalLongitude / pullDatalist.size

            val mapView: MapView = findViewById(R.id.map_view)
            mapView.start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                    }
                    override fun onMapError(error: Exception) {
                    }
                },
                object : KakaoMapReadyCallback() {
                    override fun onMapReady(kakaoMap: KakaoMap) {
                        kakaoMap.setOnLabelClickListener { kakaoMap, layer, label ->
                            val clickedItem = pullDatalist.find {
                                it.latitude.toDouble() == label.position.latitude && it.longitude.toDouble() == label.position.longitude
                            }
                            if (clickedItem != null) {
                                // Handle marker click
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                                val message = "${clickedItem.title} 클릭되었습니다."
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                                // Update views inside the bottom sheet
                                val mapImage = findViewById<ImageView>(R.id.map_item_image)
                                val imageUrl = clickedItem.imageUrl
                                mapImage.load(imageUrl) {
                                    placeholder(R.drawable.icon_main)
                                    error(R.drawable.icon_no_image)
                                }

                                val titleTextView = findViewById<AppCompatTextView>(R.id.map_item_title)
                                titleTextView.text = clickedItem.title

                                val detailMoveButton = findViewById<Button>(R.id.move_to_detail_button)
                                detailMoveButton.setOnClickListener {
                                    startActivity(
                                        Intent(
                                            this@SearchActivity, TourDetailActivity::class.java
                                        ).apply {
                                            putExtra("contentId", clickedItem.contentId)
                                        }
                                    )
                                }

                                val addressTextView = findViewById<AppCompatTextView>(R.id.map_item_address)
                                addressTextView.text = clickedItem.address
                                // Other processing (e.g., button click events, etc.)
                            } else {
                                val message = "클릭한 라벨에 대한 정보를 찾을 수 없음"
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        val position = LatLng.from(
                            centerLatitude, centerLongitude
                        )
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(position)
                        val zoomLevel = CameraUpdateFactory.zoomTo(10)
                        kakaoMap.run {
                            moveCamera(zoomLevel)
                            moveCamera(cameraUpdate)
                        }

                        for ((idx, entity) in pullDatalist.withIndex()) {
                            val latitude = entity.latitude.toDouble()
                            val longitude = entity.longitude.toDouble()
                            val markerLatLng = LatLng.from(latitude, longitude)

                            val options = LabelOptions.from(markerLatLng)
                            options.setStyles(
                                LabelStyle.from(R.drawable.icon_end_marker).setZoomLevel(10)
                            )

                            // Text label style configuration (display at all levels)
                            val labelStyles = LabelStyles.from(
                                LabelStyle.from(R.drawable.icon_end_marker).setTextStyles(32, Color.BLACK, 1, Color.GRAY)
                            )
                            options.setStyles(labelStyles)
                            options.setTexts(entity.title)
                            val layer = kakaoMap.labelManager?.layer
                            val label: Label = layer!!.addLabel(options)
                        }
                    }
                }
            )
        }
    }
}
