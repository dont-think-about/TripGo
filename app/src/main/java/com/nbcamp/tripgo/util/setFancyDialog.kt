package com.nbcamp.tripgo.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.nbcamp.tripgo.R
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog

@SuppressLint("ResourceAsColor")
fun setFancyDialog(
    context: Context,
    title: String,
    message: String,
    positiveText: String,
    negativeText: String,
    @DrawableRes icon: Int,
    onPositiveClicked: () -> Unit
): FancyAlertDialog = FancyAlertDialog.Builder
    .with(context)
    .setBackgroundColorRes(R.color.main)
    .setTitle(title)
    .setMessage(message)
    .setPositiveBtnText(positiveText)
    .onPositiveClicked {
        onPositiveClicked()
        it.dismiss()
    }
    .setPositiveBtnBackground(R.color.main)
    .setNegativeBtnText(negativeText)
    .onNegativeClicked {
        it.dismiss()
    }
    .setNegativeBtnBackground(R.color.white)
    .isCancellable(true)
    .setAnimation(Animation.SLIDE)
    .setIcon(icon, View.VISIBLE)
    .build()
