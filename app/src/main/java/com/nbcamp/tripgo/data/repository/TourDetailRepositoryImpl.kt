package com.nbcamp.tripgo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toCalendarEntity
import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toDetailCommonEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.view.tour.detail.TourDetailRepository
import kotlinx.coroutines.tasks.await

class TourDetailRepositoryImpl(
    private val tourApiService: TourApiService
) : TourDetailRepository {

    private val fireStore = FirebaseFirestore.getInstance()

    override suspend fun getDetailInformation(contentId: String?): DetailCommonEntity? {
        if (contentId == null)
            return null
        val response = tourApiService.getDetailInformation(
            contentId = contentId
        )

        if (response.isSuccessful) {
            response.body()?.let { detailModel ->
                val items = detailModel.response.body.items.item.first()
                return items.toDetailCommonEntity()
            }
        }
        return null
    }

    override suspend fun setMySchedule(
        festivalItem: FestivalItem?,
        keywordItem: KeywordItem?,
        detailInfo: DetailCommonEntity,
        startDate: String,
        endDate: String,
        email: String?
    ) {
        val plan = festivalItem?.toCalendarEntity(
            startDate,
            endDate,
            detailInfo
        ) ?: keywordItem?.toCalendarEntity(
            startDate,
            endDate,
            detailInfo
        )
        if (email == null || plan == null) {
            return
        }

        fireStore.runTransaction { transaction ->
            val calendarScheduleReference =
                fireStore.collection("calendar").document(email).collection("plans").document()
            transaction.set(
                calendarScheduleReference,
                plan
            )
        }.await()
    }

}
