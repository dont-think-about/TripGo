package com.nbcamp.tripgo.view.main

import com.nbcamp.tripgo.view.calendar.CalendarFragment
import com.nbcamp.tripgo.view.home.HomeFragment
import com.nbcamp.tripgo.view.mypage.MyPageFragment
import com.nbcamp.tripgo.view.review.WholeReviewFragment

enum class FragmentPageType(
    val title: String,
    val tag: String
) {
    PAGE_HOME("home", HomeFragment.TAG),
    PAGE_CALENDAR("calendar", CalendarFragment.TAG),
    PAGE_REVIEW("review", WholeReviewFragment.TAG),
    PAGE_MY("my", MyPageFragment.TAG),
}
