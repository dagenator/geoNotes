package com.zotreex.sample_project

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.geometry.Point
import com.zotreex.sample_project.domain.repository.YandexServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class NoteSevice : Service() {

    @Inject
    lateinit var repository: YandexServiceRepository


    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var notificationController: NotificationController

    var handler = Handler()
    var runnable: Runnable? = null
    var delay = 10000


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        (applicationContext as MainApplication).appComponent.inject(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationController = NotificationController(this)
        super.onCreate()

        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            Log.i("test", "onCreate: This method is run every 10 seconds")
            getUserPoint { x -> userLocationCallback(x) }

        }.also { runnable = it }, delay.toLong())

        this.startForeground(
            345,
            notificationController.createNotificationForService("GeoNote work").build()
        )
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


//        val input = intent?.getStringExtra(SyncStateContract.Constants.inputExtra)
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
//// 1
//        val notification = NotificationCompat.Builder(this, channelID)
//            .setContentTitle(notificationTitle)
//            .setContentText(input)
//            .setSmallIcon(R.drawable.ic_android_24dp)
//            .setContentIntent(pendingIntent)
//            .build()
//        startForeground(1, notification)
// 2
        return START_NOT_STICKY
    }


    fun calculateTheDistance(
        shir_A: Double,
        dolg_A: Double,
        shir_B: Double,
        dolg_B: Double
    ): Double {
        val lat1 = shir_A * Math.PI / 180
        val lat2 = shir_B * Math.PI / 180
        val long1 = dolg_A * Math.PI / 180
        val long2 = dolg_B * Math.PI / 180
        val cl1 = Math.cos(lat1)
        val cl2 = Math.cos(lat2)
        val sl1 = Math.sin(lat1)
        val sl2 = Math.sin(lat2)
        val delta = long2 - long1
        val cdelta = Math.cos(delta)
        val sdelta = Math.sin(delta)
        val y = Math.sqrt(
            Math.pow(
                cl2 * sdelta,
                2.0
            ) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2.0)
        )
        val x = sl1 * sl2 + cl1 * cl2 * cdelta
        val ad = Math.atan2(y, x)
        return ad * 6372795
    }

    fun getUserPoint(callback: (Point) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnCompleteListener {
                callback(Point(it.result.latitude, it.result.longitude))
            }
        }
    }

    private fun userLocationCallback(userPoint: Point) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getAllNotes().forEach {
                Log.i("distance", "user: ${userPoint.latitude}, ${userPoint.longitude}")
                Log.i("distance", "note: ${it}")
                val distance = calculateTheDistance(
                    userPoint.latitude,
                    userPoint.longitude,
                    it.latitude,
                    it.longtitude
                )
                Log.i("distance", "userLocationCallback: ${distance}")
                if (distance < 100) {
                    if (!it.isNotificated) {
                        notificationController.notify(it.note)
                        repository.noteisNotificated(it, true)
                    }
                }
            }
        }
    }

}

