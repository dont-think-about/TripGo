package com.nbcamp.tripgo.view.mypage.favorite

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.nbcamp.tripgo.R

class MypageAppInpo : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_app_inpo, null)

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("닫기") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}
