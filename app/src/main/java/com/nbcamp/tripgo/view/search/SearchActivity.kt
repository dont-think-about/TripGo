package com.nbcamp.tripgo.view.search

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySearchBinding
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.poi.TMapPOIItem

class SearchActivity : AppCompatActivity() {

    lateinit var mViewPagerAdapter: ViewPagerAdapter
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

        // RecyclerView 초기화 및 어댑터 설정
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

            val linearLayoutTmap = findViewById<LinearLayout>(R.id.linearLayoutTmap)
            linearLayoutTmap.visibility = View.VISIBLE

            val tMapView = TMapView(this)
            tMapView.setSKTMapApiKey(BuildConfig.SK_OPEN_API_KEY)
            linearLayoutTmap.addView(tMapView)
            val totalLatitude = pullDatalist.map { it.latitude.toDouble() }.sum()
            val totalLongitude = pullDatalist.map { it.longitude.toDouble() }.sum()
            val centerLatitude = totalLatitude / pullDatalist.size
            val centerLongitude = totalLongitude / pullDatalist.size

            if (!centerLatitude.isNaN() && !centerLongitude.isNaN()) {
                tMapView.setOnMapReadyListener {
                    tMapView.setCenterPoint(centerLatitude, centerLongitude)
                    tMapView.zoomLevel = 11

                    for ((idx, entity) in pullDatalist.withIndex()) {
                        val latitude = entity.latitude.toDouble()
                        val longitude = entity.longitude.toDouble()

                        val tMapPoint = TMapPoint(latitude, longitude)

                        val bitmap =
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.ic_launcher_foreground
                            )
//                    markerItem.icon = bitmap

                        val markerItem = TMapMarkerItem()
                        markerItem.id = "$idx"
                        markerItem.setPosition(0.5f, 0.5f)
                        markerItem.tMapPoint = tMapPoint
                        markerItem.name = entity.title
                        tMapView.addTMapMarkerItem(markerItem)

                        tMapView.setOnClickListenerCallback(object : TMapView.OnClickListenerCallback {
                            override fun onPressDown(
                                markerlist: ArrayList<TMapMarkerItem>?,
                                poilist: ArrayList<TMapPOIItem>?,
                                point: TMapPoint?,
                                pointf: PointF?
                            ) {
                                if (markerlist != null && markerlist.isNotEmpty()) {
                                    val clickedMarker = markerlist[0] // 첫 번째 클릭된 마커
                                    val markerName = clickedMarker.name // 마커의 이름
                                    val markerid = clickedMarker.id

                                    // 클릭 이벤트 발생 시 토스트 메시지를 표시
                                    Toast.makeText(
                                        this@SearchActivity,
                                        "$markerid 번째의 $markerName 가 클릭됐어요",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 마커 이름 설정
                                    clickedMarker.setCalloutTitle(markerName)
                                }
                            }

                            override fun onPressUp(
                                markerlist: ArrayList<TMapMarkerItem>?,
                                poilist: ArrayList<TMapPOIItem>?,
                                point: TMapPoint?,
                                pointf: PointF?
                            ) {
                            }
                        })
                    }
                }
            }
        }
    }
}
