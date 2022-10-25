package com.zotreex.sample_project

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionsUtils @Inject constructor() {

    fun checkOrAskPermission(
        activity: Activity,
        permissions: String,
        requestCode: Int,
        callback: () -> Unit
    ) {
        val permission = ContextCompat.checkSelfPermission(activity.baseContext, permissions)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            callback.invoke()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permissions),
                requestCode
            )
        }
    }

    fun askFineLocation(callback: () -> Unit, activity: Activity) {

        Log.i("permissionCheck", "requestLocationPermission: 3")
        checkOrAskPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION,
            MainActivity.PERMISSIONS_REQUEST_FINE_LOCATION
        ) {
            askCoarseLocation(callback, activity)
        }
    }

    fun askCoarseLocation(callback: () -> Unit, activity: Activity) {
        Log.i("permissionCheck", "requestLocationPermission: 2")
        checkOrAskPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            MainActivity.PERMISSIONS_REQUEST_COARSE_LOCATION
        ) { askBackground(callback, activity) }
    }

    fun askBackground(callback: () -> Unit, activity: Activity) {
        Log.i("permissionCheck", "requestLocationPermission: 3")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkOrAskPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                MainActivity.PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION
            ) { callback.invoke() }
        } else {
            callback.invoke()
        }
    }

}