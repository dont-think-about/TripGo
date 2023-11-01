package com.nbcamp.tripgo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nbcamp.tripgo.util.MapConverter.toModelMap
import com.nbcamp.tripgo.view.review.whole.ReviewRepository
import com.nbcamp.tripgo.view.reviewwriting.ReviewWritingModel
import kotlinx.coroutines.tasks.await

class ReviewRepositoryImpl : ReviewRepository {
    private val fireStore = FirebaseFirestore.getInstance()

    override suspend fun getAllReviews(): List<ReviewWritingModel> {
        val reviewList = arrayListOf<ReviewWritingModel>()
        val reviewDocuments = fireStore.collection("reviews")
            .get()
            .await()

        reviewDocuments.documents.forEach { documents ->
            val reviews = documents.reference.collection("review").get().await()
            for (document in reviews.documents) {
                val model = document.data?.toModelMap<ReviewWritingModel>() ?: continue
                reviewList.add(model)
            }
        }
        if (reviewList.isEmpty()) {
            return emptyList()
        }
        return reviewList
    }
}
