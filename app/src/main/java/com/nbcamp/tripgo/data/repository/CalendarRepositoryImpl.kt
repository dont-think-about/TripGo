package com.nbcamp.tripgo.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
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
