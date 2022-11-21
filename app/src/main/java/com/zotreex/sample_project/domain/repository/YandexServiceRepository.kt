package com.zotreex.sample_project.domain.repository

import android.util.Log
import com.zotreex.sample_project.domain.api.YandexService
import com.zotreex.sample_project.domain.data.bd.GeoNotesDatabase
import com.zotreex.sample_project.domain.data.models.GeoNote
import com.zotreex.sample_project.domain.data.models.Geocode
import com.zotreex.sample_project.domain.data.models.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject


class YandexServiceRepository @Inject constructor(
    val yandexService: YandexService,
    val geoNotesDatabase: GeoNotesDatabase,
) {

    suspend fun getGeocode(lat: Double, long: Double) = flow<Resource<Geocode>> {
        emit(Resource.loading())
        try {
            emit(Resource.success(yandexService.getGeocode(apikey, format, "${(long)},${(lat)}")))

        } catch (e: Exception) {
            emit(Resource.error(null, e.message ?: "error occurred"))
        }
    }

    suspend fun saveNewNote(address: String, lat: Double, long: Double, note: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geoNote = GeoNote(null, address, lat, long, note)
            geoNotesDatabase.getNoteDAO().insert(geoNote)
        }

    }

    suspend fun updateNewNote(address: String, lat: Double, long: Double, note: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geoNote = GeoNote(null, address, lat, long, note)
            geoNotesDatabase.getNoteDAO().update(geoNote)
        }

    }

    suspend fun updateNote(geoNote: GeoNote) {
        CoroutineScope(Dispatchers.IO).launch {
            geoNotesDatabase.getNoteDAO().update(geoNote)
        }
    }

    suspend fun noteisNotificated(geoNote: GeoNote, isNotificated: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            geoNote.isNotificated = isNotificated
            geoNotesDatabase.getNoteDAO().update(geoNote)
        }

    }

    suspend fun deleteNote(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            geoNotesDatabase.getNoteDAO().delete(address)
        }
    }

    suspend fun getNoteByAddress(address: String): GeoNote? {
        return CoroutineScope(Dispatchers.IO).async {
            return@async geoNotesDatabase.getNoteDAO().getByAddress(address)
        }.await()
    }

    suspend fun getAllNotes(): List<GeoNote> {
        val notes = geoNotesDatabase.getNoteDAO().getAll()
        Log.i("distance", "userLocationCallback: ${notes}")
        return notes
    }

    fun saveNewNote(geoNote: GeoNote) {
        CoroutineScope(Dispatchers.IO).launch {
            geoNotesDatabase.getNoteDAO().insert(geoNote)
        }
    }

    companion object {
        const val format = "json"
        const val apikey = "c7be3e52-3a31-46f8-9ffe-310c79fd32f6"
    }
}