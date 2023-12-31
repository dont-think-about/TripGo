package com.nbcamp.tripgo.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.view.App
import com.nbcamp.tripgo.view.calendar.CalendarRepository
import kotlinx.coroutines.tasks.await

class CalendarRepositoryImpl(
    context: Context
) : CalendarRepository {
//    init {
//        KakaoSdk.init(context, BuildConfig.KAKAO_API_KEY)
//        UserApiClient.instance.me { user, error ->
//            user?.let {
//                kaKaoAccount = user.kakaoAccount
//            }
//        }
//    }

    //    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    //    private var kaKaoAccount: Account? = null
    override fun getCurrentUser(): Any? {
//        if (firebaseAuth.currentUser != null && kaKaoAccount == null)
//            return firebaseAuth.currentUser
//        else if (firebaseAuth.currentUser == null && kaKaoAccount != null)
//            return kaKaoAccount
//        return null
        if (App.kakaoUser == null)
            return App.firebaseUser
        else if (App.firebaseUser == null)
            return App.kakaoUser
        return null
    }

    override suspend fun getMySchedules(email: String): List<CalendarEntity> {
        val list = fireStore.collection("calendar")
            .document(email)
            .collection("plans")
            .get()
            .await()
            .map { it.toObject<CalendarEntity>() }
        if (list.isEmpty()) {
            return emptyList()
        }
        return list
    }

    override suspend fun deleteSchedule(id: String): List<CalendarEntity> {
        val email = if (App.firebaseUser == null) {
            App.kakaoUser?.email
        } else {
            App.firebaseUser?.email
        }
        if (email == null) {
            return emptyList()
        }
        // 삭제하고
        fireStore.runTransaction { transaction ->
            val scheduleDocument = fireStore.collection("calendar")
                .document(email)
                .collection("plans")
                .document(id)

            transaction.delete(scheduleDocument)
        }.await()

        // 다시 가져 와서 리스트 업데이트
        val scheduleList = getMySchedules(email)

        if (scheduleList.isEmpty()) {
            return emptyList()
        }
        return scheduleList
    }

    override suspend fun modifySchedule(
        entity: CalendarEntity,
        startDate: String,
        endDate: String
    ): List<CalendarEntity> {
        val email = if (App.firebaseUser == null) {
            App.kakaoUser?.email
        } else {
            App.firebaseUser?.email
        }
        if (email == null) {
            return emptyList()
        }
        if (entity.id == null) {
            return emptyList()
        }

        fireStore.runTransaction { transaction ->
            val modifyEntity = entity.copy(
                startDate = startDate,
                endDate = endDate
            )

            val modifyDocumentReference = fireStore.collection("calendar")
                .document(email)
                .collection("plans")
                .document(entity.id)

            transaction.update(
                modifyDocumentReference,
                modifyEntity.toMap()
            )
        }.await()

        // 다시 가져 와서 리스트 업데이트
        val scheduleList = getMySchedules(email)

        if (scheduleList.isEmpty()) {
            return emptyList()
        }
        return scheduleList
    }
}
