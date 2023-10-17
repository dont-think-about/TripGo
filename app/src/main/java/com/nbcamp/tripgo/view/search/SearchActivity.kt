package com.nbcamp.tripgo.view.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nbcamp.tripgo.databinding.ActivitySearchBinding
import com.nbcamp.tripgo.view.search.adapters.ViewPagerAdapter



class SearchActivity : AppCompatActivity() {

    lateinit var mViewPagerAdapter: ViewPagerAdapter

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.searchViewpager.adapter = mViewPagerAdapter

        //탭 레이아웃을 => 뷰페이저와 연결.
        binding.searchTabLayout.setupWithViewPager(binding.searchViewpager)

    }
}








