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
                fireStore.collection("calendar")
                    .document(email)
                    .collection("plans")
                    .document()
            transaction.set(
                calendarScheduleReference,
                plan
            )
        }.await()
    }

    override suspend fun getAverageRatingThisPlace(contentId: String?): List<Float> {
        val userList = arrayListOf<String>()
        val ratingList = arrayListOf<Float>()

        // 유저 정보를 전부 가져와서
        val userReference = fireStore.collection("user")
            .get()
            .await()

        userReference.documents.forEach { documents ->
            userList.add(documents.id)
        }

        // 유저가 작성한 모든 리뷰를 가져온다
        userList.forEach {
            val reviewReference =
                fireStore.collection("reviews")
                    .document(it)
                    .collection("review")
                    .get()
                    .await()
            reviewReference.documents.forEach { document ->
                println(document.data?.entries)
                if (document.data?.get("contentId") == contentId) {
                    ratingList.add(document.data?.get("rating").toString().toFloat())
                } else {
                    // 평점 리뷰가 없으면 -1로 저장 해서 구분
                    ratingList.add(-1f)
                }
            }
        }
        return ratingList
    }

}
