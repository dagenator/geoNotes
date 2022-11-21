package com.zotreex.sample_project.domain.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class GeoNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val address: String,
    val latitude: Double,
    val longtitude: Double,
    var note: String,
    var isNotificated: Boolean = false,
    val lastNotification: String? = null,
) {
    override fun toString(): String {
        return "GeoNote(id=$id, address='$address', latitude=$latitude, longtitude=$longtitude, note='$note', isNotificated=$isNotificated, lastNotification=$lastNotification)"
    }
}

