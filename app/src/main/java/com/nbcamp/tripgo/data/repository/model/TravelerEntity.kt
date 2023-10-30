package com.nbcamp.tripgo.data.repository.model

data class TravelerEntity(
    // 시도 명
    val districtName: String,
    // 방문객 수
    val travelCount: Int,
    // 관광객 구분명
    val travelerIdentifier: String,
)
