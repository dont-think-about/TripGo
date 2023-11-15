package com.nbcamp.tripgo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toCalendarEntity
import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toDetailCommonEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.data.service.TMapApiService
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.tour.detail.TourDetailRepository
import kotlinx.coroutines.tasks.await

class TourDetailRepositoryImpl(
    private val tourApiService: TourApiService,
    private val tMapApiService: TMapApiService
) : TourDetailRepository {

    private val fireStore = FirebaseFirestore.getInstance()
    private lateinit var userInfo: String

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
        val plan = festivalItem?.toCalendarEntity(startDate, endDate, detailInfo)
            ?: keywordItem?.toCalendarEntity(startDate, endDate, detailInfo)
            ?: detailInfo.toCalendarEntity(startDate, endDate)

        if (email != null && plan != null) {
            fireStore.runTransaction { transaction ->
                // 하위 문서를 위한 더미데이터
                fireStore.collection("calendar")
                    .document(userInfo)
                    .set(mapOf("dummy" to ""))

                val calendarScheduleReference =
                    fireStore.collection("calendar")
                        .document(email)
                        .collection("plans")
                        .document()
                transaction.set(calendarScheduleReference, plan)
            }.await()
        }
    // 재민 수정했는데 오류발생할수있음 !
    fireStore.runTransaction { transaction ->
            // 하위 문서를 위한 더미데이터
            fireStore.collection("calendar")
                .document(userInfo)
                .set(mapOf("dummy" to ""))

            val calendarScheduleReference =
                fireStore.collection("calendar")
                    .document(email.toString())
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
        val userReference = fireStore.collection("users")
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

    override suspend fun saveLikePlace(
        detailInfo: DetailCommonEntity,
        contentId: String,
        currentUser: Any?
    ) {
        getUserInfo()
        fireStore.runTransaction { transaction ->
            // 하위 문서를 위한 더미데이터
            fireStore.collection("liked")
                .document(userInfo)
                .set(mapOf("dummy" to ""))

            val likedReference = fireStore.collection("liked")
                .document(userInfo)
                .collection("like")
                .document()

            val likedModel = hashMapOf(
                "contentId" to contentId,
                "title" to detailInfo.title,
                "description" to detailInfo.description,
                "telPhoneNumber" to detailInfo.telPhoneNumber,
                "homePage" to detailInfo.homePage,
                "mainAddress" to detailInfo.mainAddress,
                "subAddress" to detailInfo.subAddress,
                "imageUrl" to detailInfo.imageUrl,
                "latitude" to detailInfo.latitude,
                "longitude" to detailInfo.longitude
            )

            transaction.set(
                likedReference,
                likedModel
            )
        }.await()
    }

    override suspend fun removeLikePlace(contentId: String, currentUser: Any?) {
        getUserInfo()
        val usersLikedList = fireStore.collection("liked")
            .document(userInfo)
            .collection("like")

        var documentId = ""
        usersLikedList
            .get()
            .await()
            .documents.forEach { document ->
                if (document.data?.get("contentId") == contentId) {
                    documentId = document.id
                }
            }
        fireStore.runTransaction { transaction ->
            val documentReference = usersLikedList.document(documentId)
            transaction.delete(documentReference)
        }
    }

    override suspend fun getLikedStatusThisContent(contentId: String): Boolean {
        getUserInfo()
        val usersLikedList = fireStore.collection("liked")
            .document(userInfo)
            .collection("like")
        usersLikedList
            .get()
            .await()
            .documents.forEach { document ->
                if (document.data?.get("contentId") == contentId) {
                    return true
                }
            }
        return false
    }

    private fun getUserInfo() {
        if (App.kakaoUser == null) {
            userInfo = App.firebaseUser?.email.toString()
        } else if (App.firebaseUser == null) {
            userInfo = App.kakaoUser?.email.toString()
        }
    }
}
