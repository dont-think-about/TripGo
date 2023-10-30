package com.nbcamp.tripgo.util

import android.content.Context
import androidx.lifecycle.LifecycleOwner

class DetectNetwork(
    private val context: Context
) {

    private var loadingDialog: LoadingDialog = LoadingDialog(context)
    fun detectNetworkStatus(
        owner: LifecycleOwner
    ) {

        // 인터넷 연결 감지용 networkCallback observing
        ConnectWatcher(context).observe(owner) {
            when (it) {
                false -> {
                    loadingDialog.run {
                        setVisible()
                        setText("인터넷 상태를 확인해주세요.")
                    }
                }

                true -> {
                    loadingDialog.setInvisible()
                }
            }
        }
    }
}
