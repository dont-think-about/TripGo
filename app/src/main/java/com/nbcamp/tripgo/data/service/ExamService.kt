package com.nbcamp.tripgo.data.service

import com.nbcamp.tripgo.data.model.ExamResponseModel

// retrofit 연결 하는 서비스 (인터페이스)
interface ExamService {
    suspend fun getInfo(): ExamResponseModel
}
