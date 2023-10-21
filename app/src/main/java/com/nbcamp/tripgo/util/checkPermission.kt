package com.nbcamp.tripgo.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.nbcamp.tripgo.view.main.MainActivity

fun checkPermission(
    context: Context,
    permission: String,
    permissionLauncher: ActivityResultLauncher<String>,
    showPermissionContextPopUp: () -> Unit,
    runTaskAfterPermissionGranted: () -> Unit
) {
    when {
        ActivityCompat.checkSelfPermission(
            context as MainActivity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED -> {
            runTaskAfterPermissionGranted()
        }
        // 위치 권한 안내가 필요 하면
        ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            permission
        ) -> {
            showPermissionContextPopUp()
        }
        // 그외
        else -> {
            permissionLauncher.launch(permission)
        }
    }
}
