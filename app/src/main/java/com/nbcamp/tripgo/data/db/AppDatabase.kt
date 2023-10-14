package com.nbcamp.tripgo.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database 추가 해야함
abstract class AppDatabase : RoomDatabase() {

    // DAO 추가 해야함

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context, AppDatabase::class.java, "trip_go"
                ).build()
            }
            return INSTANCE as AppDatabase
        }
    }
}
