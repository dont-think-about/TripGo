package com.nbcamp.tripgo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData

class ConnectWatcher(
    context: Context
) : LiveData<Boolean>() {

    // 네트워크 상태를 살피기 위한 네트워크 콜백
    private val networkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            // 정상이라면 라이브데이터에 true 전달
            override fun onAvailable(network: Network) {
                postValue(true)
            }

            override fun onLost(network: Network) {
                postValue(false)
            }
        }
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // lifecycle이 활성화 되어 있을 때 불리는 콜백
    // network callback을 등록 해서 네트워크 상태를 관찰한다.
    override fun onActive() {
        postValue(updateConnection())
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    // lifecycle이 비 활성화 되어 있을 때 불리는 콜백
    //  network callback을 제거한다.
    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    // network callback은 상태가 변경 될 때만, 콜백이 호출 된다.
    // 그래서 초기 상태를 세팅해서 알려 상태를 알려 주어야한다.
    private fun updateConnection(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val newCapabilities =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        val result = when {
            newCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            newCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            newCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }
}
