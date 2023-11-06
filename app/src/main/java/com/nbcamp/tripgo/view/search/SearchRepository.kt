package com.nbcamp.tripgo.view.search

import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.festivals.FestivalResponseModel
import com.nbcamp.tripgo.data.model.travelerssegmentation.SegmentationItem
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity

interface SearchRepository {
    /**
     * @param keyword 검색 요청 할 키워드
     * @param contentTypeId 관광 타입 (12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점) ID
     * @param responseCount 반환할 결과 수
     *
     * @return 키워드와 관광 타입에 따른 관광지 목록
     */
    suspend fun getPlaceBySearch(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): List<KeywordSearchEntity>?

    suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): List<SegmentationItem>?

    suspend fun getFestivalBySearch(
        startDate: String,
        responseCount: Int
    ): List<FestivalItem>?
}
