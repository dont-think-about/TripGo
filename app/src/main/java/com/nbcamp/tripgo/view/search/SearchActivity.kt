package com.nbcamp.tripgo.view.search

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.databinding.ActivitySearchBinding
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter

class SearchActivity : AppCompatActivity(){

    private var searchItems = ArrayList<SearchItemModel>()
    private var spinnerMap= ArrayList<HashMap<String,String>>()
    private var categoryName = ArrayList<String>()

    lateinit var mViewPagerAdapter: ViewPagerAdapter
    private lateinit var binding: ActivitySearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private val searchViewModel : SearchViewModel by viewModels()

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
        searchViewModel.pullData.observe(this){ pullDatalist ->
            adapter.clearItem()
            adapter.additem(pullDatalist)
            Log.d("키워드 123", "값 = $pullDatalist")
        }
    }
}
