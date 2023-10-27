package com.nbcamp.tripgo.view.tour.detail

import android.graphics.Bitmap
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity

interface TourDetailRepository {

    suspend fun getDetailInformation(contentId: String?): DetailCommonEntity?

    suspend fun setMySchedule(
        festivalItem: FestivalItem?,
        keywordItem: KeywordItem?,
        detailInfo: DetailCommonEntity,
        startDate: String,
        endDate: String,
        email: String?
    )

    suspend fun getAverageRatingThisPlace(
        contentId: String?
    ): List<Float>

    suspend fun getRouteImage(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): Bitmap?

    suspend fun saveLikePlace(
        detailInfo: DetailCommonEntity,
        contentId: String,
        currentUser: Any?
    )

    suspend fun removeLikePlace(
        contentId: String,
        currentUser: Any?
    )

    suspend fun getLikedStatusThisContent(
        contentId: String
    ): Boolean

}
