package com.nbcamp.tripgo.util.extension

import android.content.Context
import android.widget.Toast

/**
 *  activity에서는 toast("123") 형식으로 사용 가능
 *  fragment에서는 requireActivity.toast("123") 형식으로 사용 가능
 */
object ContextExtension {
    fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
