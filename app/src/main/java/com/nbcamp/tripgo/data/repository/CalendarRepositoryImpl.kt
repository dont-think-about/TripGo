package com.nbcamp.tripgo.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nbcamp.tripgo.R
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.util.APIResponse
import com.nbcamp.tripgo.view.calendar.CalendarRepository
import kotlinx.coroutines.tasks.await

class CalendarRepositoryImpl(
    private val context: Context
) : CalendarRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()
    override fun getCurrentUser(): FirebaseUser? {
        if (firebaseAuth.currentUser != null)
            return firebaseAuth.currentUser
        return null
    }

    override suspend fun getMySchedules(email: String?): APIResponse<List<CalendarEntity>> {
        if (email == null)
            return APIResponse.Error(context.getString(R.string.not_logged_in))
        val list = fireStore.collection("calendar")
            .document(email)
            .collection("plans")
            .get()

            .await()
            .map { it.toObject<CalendarEntity>() }
        if (list.isEmpty()) {
            return APIResponse.Error(context.getString(R.string.no_data))
        }
        return APIResponse.Success(list)
    }
}
