package com.nbcamp.tripgo.view.home

import com.nbcamp.tripgo.data.model.ExamResponseModel

// 뷰모델과 데이터 영역(retrofit 호출)을 잇는 repository 추상화
interface HomeRepository {
    suspend fun getInfo(): ExamResponseModel
}
