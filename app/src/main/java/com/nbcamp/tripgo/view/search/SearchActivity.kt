package com.nbcamp.tripgo.view.search

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySearchBinding
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem

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
            tMapView.setSKTMapApiKey("bhhiJNx9SP03zPiuLpLl5y8W4BMNuLtargbZ6ESj")
            linearLayoutTmap.addView(tMapView)
            val totalLatitude = pullDatalist.map { it.latitude.toDouble() }.sum()
            val totalLongitude = pullDatalist.map { it.longitude.toDouble() }.sum()
Log.d("pull","$pullDatalist")
            val centerLatitude = totalLatitude / pullDatalist.size
            val centerLongitude = totalLongitude / pullDatalist.size

            if (!centerLatitude.isNaN() && !centerLongitude.isNaN()) {
                tMapView.setOnMapReadyListener {
                    tMapView.setCenterPoint(centerLatitude, centerLongitude)
                    tMapView.zoomLevel = 11
                    Log.d(
                        "중심 위도 경도",
                        "Center Latitude: $centerLatitude, Center Longitude: $centerLongitude"
                    )

                    for ((idx, entity) in pullDatalist.withIndex()) {
                        val latitude = entity.latitude.toDouble()
                        val longitude = entity.longitude.toDouble()
                        Log.d("제발1", "Latitude: $latitude, Longitude: $longitude")

                        val tMapPoint = TMapPoint(latitude, longitude)

                        // 마커 아이콘
                        val bitmap =
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.ic_launcher_foreground
                            )

                        val markerItem = TMapMarkerItem()
//                    markerItem.icon = bitmap
                        markerItem.id = "$idx"
                        markerItem.setPosition(0.5f, 0.5f)
                        markerItem.tMapPoint = tMapPoint
                        markerItem.name = entity.title
                        tMapView.addTMapMarkerItem(markerItem)
                    }
                }
            }
        }
    }
}
