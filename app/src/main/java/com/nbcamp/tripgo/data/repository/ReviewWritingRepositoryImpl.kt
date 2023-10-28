package com.nbcamp.tripgo.data.repository

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.calendar.WritingType
import com.nbcamp.tripgo.view.reviewwriting.CalendarUserModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingRepository
import kotlinx.coroutines.tasks.await

class ReviewWritingRepositoryImpl : ReviewWritingRepository {
    private val storage = FirebaseStorage.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    private lateinit var userInfo: String
    private lateinit var fileName: String

    override suspend fun saveToStorage(
        reviewWritingModel: ReviewWritingModel,
        calendarUserModel: CalendarUserModel,
        writingType: WritingType
    ): String {
//        when (val user = calendarUserModel.currentUser) {
//            is FirebaseUser -> {
//                userInfo = user.email.toString()
//            }
//
//            is Account -> {
//                userInfo = user.email.toString()
//            }
//        }
        if (App.kakaoUser == null) {
            userInfo = App.firebaseUser?.email.toString()
        } else if (App.firebaseUser == null) {
            userInfo = App.kakaoUser?.email.toString()
        }

        fileName = "${userInfo.split("@").first()}_${calendarUserModel.model?.id}.png"

        // 작성 수정 상태에 따라 분기 처리
        return when (writingType) {
            WritingType.NEW -> {
                // 새로운 이미지 넣기
                saveNewImage(reviewWritingModel)
            }

            WritingType.MODIFY -> {
                // 이전 이미지 지우고 새로운 이미지 넣기
                removePastImageAndSaveNewImage(reviewWritingModel)
            }
        }
    }


    override suspend fun saveReview(
        reviewedModel: ReviewWritingModel,
        documentId: String,
        writingType: WritingType
    ) {
        // 데이터 무결성을 위해 트랜잭션 사용
        fireStore.runTransaction { transaction ->
            val reviewReference =
                fireStore.collection("reviews").document(userInfo).collection("review")
                    .document(documentId)
            when (writingType) {
                WritingType.NEW -> {
                    transaction.set(
                        reviewReference,
                        reviewedModel
                    )
                }

                WritingType.MODIFY -> {
                    transaction.update(
                        reviewReference,
                        reviewedModel.toMap(),
                    )
                }
            }

        }.await()
    }

    override suspend fun updateReviewStatus(
        reviewedModel: ReviewWritingModel,
        documentId: String,
        writingType: WritingType
    ) {
        val userScheduleReference =
            fireStore.collection("calendar").document(userInfo).collection("plans")
                .document(documentId)

        // 데이터 무결성을 위해 트랜잭션 사용
        fireStore.runTransaction { transaction ->
            val schedule = transaction.get(userScheduleReference).toObject<CalendarEntity>()
            if (schedule != null) {
                transaction.set(
                    userScheduleReference,
                    schedule.copy(
                        isReviewed = true
                    ),
                    // 바뀐 부분을 merge 한다. (기본 옵션은 덮어 쓰기)
                    SetOptions.merge()
                )
            }
        }.await()
    }

    override suspend fun setPastReviewForModifyReview(model: CalendarUserModel): ReviewWritingModel? {
        when (val user = model.currentUser) {
            is FirebaseUser -> {
                userInfo = user.email.toString()
            }

            is Account -> {
                userInfo = user.email.toString()
            }
        }

        return fireStore.collection("reviews")
            .document(userInfo)
            .collection("review")
            .document(model.model?.id ?: "")
            .get()
            .await()
            .data
            ?.toReviewWritingModel()
    }

    private suspend fun saveNewImage(reviewWritingModel: ReviewWritingModel): String {
        return storage.reference.child("reviews/${userInfo}").child(fileName)
            .putFile(reviewWritingModel.imageUrl.toUri())
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }

    private suspend fun removePastImageAndSaveNewImage(reviewWritingModel: ReviewWritingModel): String {
        // 삭제 하고 다시 집어넣기
        storage.reference.child("reviews/${userInfo}").child(fileName)
            .delete()
            .await()
        return saveNewImage(reviewWritingModel)
    }

    private fun Map<String, Any>.toReviewWritingModel(): ReviewWritingModel {
        val gson = Gson()
        val gsonString = gson.toJson(this)
        return gson.fromJson(gsonString, ReviewWritingModel::class.java)
    }

}
