package com.nbcamp.tripgo.util

import com.google.gson.Gson

object MapConverter {
    inline fun <reified T>  Map<String, Any>.toModelMap() : T {
        val gson = Gson()
        val gsonString = gson.toJson(this)
        return gson.fromJson(gsonString, T::class.java)
    }
}
