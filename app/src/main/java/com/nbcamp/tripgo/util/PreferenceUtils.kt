package com.nbcamp.tripgo.util

import android.app.Activity
import android.content.Context

// 자동로그인 확인을 할 유저 정보를 담고 있는 preference 클래스
class PreferenceUtils(context: Context) {
    private val prefName = USER_PREFS_NAME
    private val userPrefs = context.getSharedPreferences(prefName, Activity.MODE_PRIVATE)
    private val prefCountName = COUNT_PREFS_NAME
    private val countPrefs = context.getSharedPreferences(prefCountName, Activity.MODE_PRIVATE)
    var user: String?
        get() = userPrefs.getString(USER_PREFS_NAME, null)
        set(value) {
            userPrefs.edit().putString(USER_PREFS_NAME, value).apply()
        }

    var feedbackCount: Int
        get() = countPrefs.getInt(COUNT_PREFS_NAME, 0)
        set(value) {
            countPrefs.edit().putInt(COUNT_PREFS_NAME, value).apply()
        }


    fun clear() {
        userPrefs.edit().clear().apply()
        countPrefs.edit().clear().apply()
    }

    companion object {
        const val USER_PREFS_NAME = "user_prefs"
        const val COUNT_PREFS_NAME = "count_prefs"
    }
}
