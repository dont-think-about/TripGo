package com.nbcamp.tripgo.view.mypage.reviewwriting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.nbcamp.tripgo.R

class MyPageDialog(context: Context) : Dialog(context) {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_my_page_dialog)

        // XML에서 정의한 뷰 요소에 대한 참조를 가져옵니다.  fragment_my_page_dialog
        val mypage_dialog_user_imageview =
            findViewById<ImageView>(R.id.mypage_dialog_user_imageview)
        val mypage_dialog_signin_up_textview =
            findViewById<TextView>(R.id.mypage_dialog_signin_up_textview)
        val mypage_dialog_sigin_up_inpo_textview =
            findViewById<TextView>(R.id.mypage_dialog_sigin_up_inpo_textview)
        val mypage_dialog_edit_userinpo_button =
            findViewById<Button>(R.id.mypage_dialog_edit_userinpo_button)
        val mypage_dialog_usergrade_textview =
            findViewById<TextView>(R.id.mypage_dialog_usergrade_textview)
        val mypage_dialog_progressbar = findViewById<ProgressBar>(R.id.mypage_dialog_progressbar)
        val mypage_dialog_total_count_textview =
            findViewById<TextView>(R.id.mypage_dialog_total_count_textview)
        val mypage_dialog_next_grage_count_textview =
            findViewById<TextView>(R.id.mypage_dialog_next_grage_count_textview)

        mypage_dialog_edit_userinpo_button.setOnClickListener {
            // 버튼 클릭 이벤트 처리
            // 원하는 동작을 추가하세요.
        }
    }
}
