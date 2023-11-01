package com.nbcamp.tripgo.view.search

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
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
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var binding: ActivitySearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.searchViewpager.adapter = mViewPagerAdapter
        binding.searchTabLayout.setupWithViewPager(binding.searchViewpager)

        recyclerView = binding.searchRankRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter(this)
        recyclerView.adapter = adapter

        val searchBackImageView = findViewById<ImageView>(R.id.search_back)
        searchBackImageView.setOnClickListener {
            finish()
        }

        searchViewModel.pullData.observe(this) { pullDatalist ->
            adapter.clearItem()
            adapter.additem(pullDatalist)

            val totalLatitude = pullDatalist.map { it.latitude.toDouble() }.sum()
            val totalLongitude = pullDatalist.map { it.longitude.toDouble() }.sum()
            val centerLatitude = totalLatitude / pullDatalist.size
            val centerLongitude = totalLongitude / pullDatalist.size

            val removeButton = findViewById<ImageView>(R.id.remove_button)
            removeButton.setOnClickListener {
                // 레이아웃을 숨기도록 설정
                val detailLayout = findViewById<ConstraintLayout>(R.id.map_of_detail_item)
                detailLayout.visibility = View.GONE
            }

            val mapView: MapView = findViewById(R.id.map_view)
            mapView.start(object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    // 지도 API 가 정상적으로 종료될 때 호출됨
                }

                override fun onMapError(error: Exception) {
                    Log.d("오류", "$error")
                }
            }, object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    kakaoMap.setOnLabelClickListener { kakaoMap, layer, label ->
                        val clickedItem = pullDatalist.find {
                            it.latitude.toDouble() == label.position.latitude && it.longitude.toDouble() == label.position.longitude
                        }

                        if (clickedItem != null) {
                            val message = "${clickedItem.title} 클릭되었습니다."

                            // 레이아웃을 보이도록 설정
                            val detailLayout = findViewById<ConstraintLayout>(R.id.map_of_detail_item)
                            detailLayout.visibility = View.VISIBLE

                            val mapImage = findViewById<ImageView>(R.id.map_item_image)
                            val imageUrl = clickedItem.imageUrl

                            mapImage.load(imageUrl) {
                            }

                            // 나머지 처리 (예: TextView에 데이터 설정 등)
                            val titleTextView = findViewById<AppCompatTextView>(R.id.map_item_title)
                            titleTextView.text = clickedItem.title

                            val addressTextView = findViewById<AppCompatTextView>(R.id.map_item_address)
                            addressTextView.text = clickedItem.address

//                            val ratingBar = findViewById<RatingBar>(R.id.review_detail_rating_bar)
//                            ratingBar.rating = clickedItem.rating.toFloat()
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                        } else {
                            val message = "클릭한 라벨에 대한 정보를 찾을 수 없음"
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                    }


                    val position = LatLng.from(
                        centerLatitude,
                        centerLongitude
                    )
                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(position)
                    kakaoMap.moveCamera(cameraUpdate)

                    for ((idx, entity) in pullDatalist.withIndex()) {
                        val latitude = entity.latitude.toDouble()
                        val longitude = entity.longitude.toDouble()
                        val markerLatLng = LatLng.from(latitude, longitude)

                        val options = LabelOptions.from(markerLatLng)

                        options.setStyles(
                            LabelStyle.from(R.drawable.icon_end_marker).setZoomLevel(10)
                        )
                        // 텍스트 레이블 스타일 설정 (모든 레벨에서 표시)
                        val labelStyles = LabelStyles.from(
                            LabelStyle.from(R.drawable.icon_end_marker)
                                .setTextStyles(32, Color.BLACK, 1, Color.GRAY)
                        )

                        options.setStyles(labelStyles)
                        options.setTexts(entity.title)

                        val layer = kakaoMap.labelManager?.layer
                        val label: Label = layer!!.addLabel(options)

                    }
                }
            })
        }
    }
}
