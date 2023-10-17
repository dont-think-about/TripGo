package com.nbcamp.tripgo.view.home

import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.util.APIResponse

// 뷰모델과 데이터 영역(retrofit 호출)을 잇는 repository 추상화
interface HomeRepository {
    suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): APIResponse<List<TravelerEntity>>

    suspend fun getFestivalsInThisMonth(
        startDate: String,
        responseCount: Int
    ): APIResponse<List<FestivalEntity>>
}
