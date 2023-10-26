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
        if (App.kaKaoUser == null)
            return App.firebaseUser
        else if (App.firebaseUser == null)
            return App.kaKaoUser
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
}
