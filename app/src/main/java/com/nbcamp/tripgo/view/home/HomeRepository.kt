package com.nbcamp.tripgo.view.home

import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.data.repository.model.NearbyPlaceEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.data.repository.model.WeatherEntity

// 뷰모델과 데이터 영역(retrofit 호출)을 잇는 repository 추상화
interface HomeRepository {

    /**
     * @param  startDate 검색할 시작 날짜
     * @param  endDate 검색할 종료 날짜
     * @param  responseCount 반환할 결과 수
     *
     * @return 시작~종료 날짜 사이의 지자체 별 방문 객 수 목록
     */
    suspend fun getCalculationTravelers(
        startDate: String,
        endDate: String,
        responseCount: Int
    ): List<TravelerEntity>?

    /**
     * @param startDate 검색할 시작 날짜
     * @param responseCount 반환할 결과 수
     *
     * @return 시작 날짜 부터 시작하는 축제 정보 목록
     */
    suspend fun getFestivalsInThisMonth(
        startDate: String,
        responseCount: Int
    ): List<FestivalEntity>?

    /**
     * @param date 날짜
     * @param time 시간
     *
     * @return 특정 시간의 날씨 정보
     */
    suspend fun getTodayWeather(
        date: String,
        time: String
    ): WeatherEntity?

    /**
     * @param keyword 검색 요청 할 키워드
     * @param contentTypeId 관광 타입 (12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점) ID
     * @param responseCount 반환할 결과 수
     *
     * @return 키워드와 관광 타입에 따른 관광지 목록
     */
    suspend fun getInformationByKeyword(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): List<KeywordSearchEntity>?

    /**
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 위,경도 반경 거리 (m)
     * @param pageNumber 한 페이지 당 불러올 결과의 수
     *
     * @return 위,경도 반경 안에 있는 관광지 목록
     */
    suspend fun getNearbyPlaces(
        latitude: String,
        longitude: String,
        radius: String,
        pageNumber: String
    ): List<NearbyPlaceEntity>?
}
