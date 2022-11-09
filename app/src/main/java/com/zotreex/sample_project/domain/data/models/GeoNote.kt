package com.zotreex.sample_project.domain.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GeoNote(
    @PrimaryKey
    val address: String,
    val latitude: Double,
    val longtitude: Double,
    val note: String,
    var isNotificated:Boolean = false,
    val lastNotification:String? = null

)