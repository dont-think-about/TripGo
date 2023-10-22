package com.nbcamp.tripgo.data.repository

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
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
        calendarUserModel: CalendarUserModel
    ): String {
        when (val user = calendarUserModel.currentUser) {
            is FirebaseUser -> {
                userInfo = user.email.toString()
            }

            is Account -> {
                userInfo = user.email.toString()
            }
        }

        fileName = "${userInfo.split("@").first()}_${System.currentTimeMillis()}.png"
        return storage.reference.child("reviews/${userInfo}").child(fileName)
            .putFile(reviewWritingModel.imageUrl.toUri())
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }

    override suspend fun saveReview(
        reviewedModel: ReviewWritingModel
    ) {
        // 데이터 무결성을 위해 트랜잭션 사용
        fireStore.runTransaction { transaction ->
            val reviewReference =
                fireStore.collection("reviews").document(userInfo).collection("review").document()
            transaction.set(
                reviewReference,
                reviewedModel
            )
        }.await()
    }

    override suspend fun updateReviewStatus(
        reviewedModel: ReviewWritingModel,
        documentId: String
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
                    SetOptions.merge()
                )
            }
        }.await()
    }
}
