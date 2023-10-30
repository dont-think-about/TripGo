package com.nbcamp.tripgo.view.search.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.nbcamp.tripgo.view.search.Festival.FestivalFragment
import com.nbcamp.tripgo.view.search.attaction.AttractionsFragment
import com.nbcamp.tripgo.view.search.tour.TourFragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "축제"
            1 -> "관광지"
            else -> "문화 생활"
        } // 각 페이지마다 제목 설정
    }

    override fun getCount(): Int {
        return 3
    } // 프레그먼트 갯수

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FestivalFragment()
            1 -> TourFragment()
            else -> AttractionsFragment()
        }
    } // 나타낼 페이지 순서 지정
}
