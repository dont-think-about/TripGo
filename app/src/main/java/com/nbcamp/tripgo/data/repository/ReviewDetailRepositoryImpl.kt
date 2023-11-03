package com.nbcamp.tripgo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.data.repository.model.UserModel
import com.nbcamp.tripgo.util.MapConverter.toModelMap
import com.nbcamp.tripgo.view.review.detail.ReviewDetailRepository
import com.nbcamp.tripgo.view.review.detail.UserStatus
import kotlinx.coroutines.tasks.await

class ReviewDetailRepositoryImpl : ReviewDetailRepository {
    private val fireStore = FirebaseFirestore.getInstance()
    override suspend fun getUserStatus(userInfo: String?): UserStatus? {
        if (userInfo == null) {
            return null
        }

        val userDocuments = fireStore.collection("users")
            .get()
            .await()

        userDocuments.documents.forEach { documents ->
            val model = documents.data?.toModelMap<UserModel>()
            if (model?.nickname == userInfo) {
                return UserStatus(
                    email = model.email,
                    nickname = model.nickname,
                    profileImage = model.profileImage,
                    reviewCount = model.reviewCount
                )
            }
        }

        return null
    }
}
