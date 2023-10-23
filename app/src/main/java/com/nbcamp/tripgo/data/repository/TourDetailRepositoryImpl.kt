package com.nbcamp.tripgo.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.Account
import com.nbcamp.tripgo.BuildConfig
import com.nbcamp.tripgo.data.repository.mapper.DetailMapper.toDetailCommonEntity
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity
import com.nbcamp.tripgo.data.service.TourApiService
import com.nbcamp.tripgo.view.tour.detail.TourDetailRepository
import kotlinx.coroutines.tasks.await

class TourDetailRepositoryImpl(
    context: Context,
    private val tourApiService: TourApiService
) : TourDetailRepository {
    init {
        KakaoSdk.init(context, BuildConfig.KAKAO_API_KEY)
        UserApiClient.instance.me { user, error ->
            user?.let {
                kaKaoAccount = user.kakaoAccount
            }
        }
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    private var kaKaoAccount: Account? = null
    override fun getCurrentUser(): Any? {
        if (firebaseAuth.currentUser != null && kaKaoAccount == null)
            return firebaseAuth.currentUser
        else if (firebaseAuth.currentUser == null && kaKaoAccount != null)
            return kaKaoAccount
        return null
    }

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

    override suspend fun getMySchedules(email: String): List<CalendarEntity> {
        return fireStore.collection("calendar").document(email)
            .collection("plans")
            .get()
            .await()
            .map { it.toObject() }
    }

}
