package com.nbcamp.tripgo.view.tour

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.service.RetrofitModule
import com.nbcamp.tripgo.databinding.ActivityTourBinding
import com.nbcamp.tripgo.view.home.valuetype.TourTheme
import com.nbcamp.tripgo.view.tour.adapter.TourAdapter
import com.nbcamp.tripgo.view.tour.adapter.TourSearchAdapter
import com.nbcamp.tripgo.view.tour.detail.TourDetailActivity
import kotlinx.coroutines.launch

class TourActivity : AppCompatActivity() {
    private var tourTheme: Int = 0

    private val binding: ActivityTourBinding by lazy {
        ActivityTourBinding.inflate(layoutInflater)
    }

    private val tourSearchAdapter: TourSearchAdapter by lazy {
        TourSearchAdapter { tourItem -> gotoDetailActivity(null, tourItem) }
    }


    private val tourAdapter: TourAdapter by lazy {
        TourAdapter { festivalItem -> gotoDetailActivity(festivalItem, null) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.tourRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        tourTheme = intent.getIntExtra("theme", 0)
//
//        } else {
//            tourTheme = intent.getIntExtra("theme")
//
//        }

        initView()

    }

    private fun initView() {

        when (tourTheme) {
            TourTheme.FAMILY.themeId -> {

                retrofitThemeSearch("가족")

            }

            TourTheme.HEALING.themeId -> {

                retrofitThemeSearch("힐링")


            }

            TourTheme.CAMPING.themeId -> {
                retrofitThemeSearch("캠핑")

            }

            TourTheme.TASTY.themeId -> {

                retrofitThemeSearch("맛")


            }

            TourTheme.POPULAR.themeId -> {

                retorifitwork()

            }

            TourTheme.NEARBY.themeId -> Unit
            TourTheme.SEARCH.themeId -> Unit
            else -> {}
        }

    }

    private fun retrofitThemeSearch(keyword: String) {
        binding.tourRecyclerview.adapter = tourSearchAdapter
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()

            try {
                val response = service.getPlaceBySearch(
                    keyword = keyword,
                    contentTypeId = "",
                    responseCount = 100
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        println(festivals)
                        tourSearchAdapter.submitList(festivals.toMutableList())
                    }
                } else {
                    showError("행사 정보를 가져오는데 실패했습니다.")
                }
            } catch (e: Exception) {
                showError("행사 정보를 가져오는 도중 오류가 발생했습니다: ${e.localizedMessage}")
                println(e.localizedMessage)
            }

        }
    }


    private fun retorifitwork() {
        binding.tourRecyclerview.adapter = tourAdapter
        lifecycleScope.launch {
            val service = RetrofitModule.createTourApiService()

            try {
                val response = service.getFestivalInThisMonth(
                    startDate = "20231001",
                    responseCount = 50
                )
                if (response.isSuccessful && response.body() != null) {
                    val festivals = response.body()?.response?.body?.items?.item
                    if (festivals != null) {
                        tourAdapter.submitList(festivals)
                    }
                } else {
                    showError("행사 정보를 가져오는데 실패했습니다.")
                }
            } catch (e: Exception) {
                showError("행사 정보를 가져오는 도중 오류가 발생했습니다: ${e.localizedMessage}")
                println(e.localizedMessage)
            }

        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun gotoDetailActivity(festivalItem: FestivalItem?, keywordItem: KeywordItem?) {
        val myIntent = Intent(this, TourDetailActivity::class.java)
            .apply {
                putExtra("festivalItem", festivalItem)
                putExtra("keywordItem", keywordItem)
            }
        startActivity(myIntent)
    }

}

