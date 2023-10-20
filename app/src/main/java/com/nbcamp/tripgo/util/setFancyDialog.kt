package com.nbcamp.tripgo.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.nbcamp.tripgo.R
import com.shashank.sony.fancydialoglib.Animation
import com.shashank.sony.fancydialoglib.FancyAlertDialog

@SuppressLint("ResourceAsColor")
fun setFancyDialog(
    context: Context,
    onPositiveClicked: () -> Unit
): FancyAlertDialog = FancyAlertDialog.Builder
    .with(context)
    .setBackgroundColorRes(R.color.main)
    .setTitle("리뷰 작성")
    .setMessage("리뷰 작성을 하시겠나요?")
    .setPositiveBtnText("예")
    .onPositiveClicked {
        onPositiveClicked()
        it.dismiss()
    }
    .setPositiveBtnBackground(R.color.main)
    .setNegativeBtnText("아니요")
    .onNegativeClicked {
        it.dismiss()
    }
    .setNegativeBtnBackground(R.color.white)
    .isCancellable(true)
    .setAnimation(Animation.SLIDE)
    .setIcon(R.drawable.icon_alert_review, View.VISIBLE)
    .build()
