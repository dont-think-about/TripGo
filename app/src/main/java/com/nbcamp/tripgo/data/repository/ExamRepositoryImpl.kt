package com.nbcamp.tripgo.data.repository

import com.nbcamp.tripgo.data.model.ExamResponseModel
import com.nbcamp.tripgo.data.service.ExamService
import com.nbcamp.tripgo.view.home.HomeRepository

// 뷰모델과 데이터 영역(retrofit 호출)을 잇는 repository 구현체
class ExamRepositoryImpl(
    private val examService: ExamService
) : HomeRepository {
    override suspend fun getInfo(): ExamResponseModel {
        return examService.getInfo()
    }
}
