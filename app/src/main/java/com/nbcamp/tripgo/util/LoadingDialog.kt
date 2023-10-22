package com.nbcamp.tripgo.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.nbcamp.tripgo.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context) {

    private var binding: DialogLoadingBinding

    init {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun setVisible() {
        this.show()
    }

    fun setInvisible() {
        this.dismiss()
    }

    fun setText(message: String) = with(binding) {
        progressTextView.text = message
    }
}
