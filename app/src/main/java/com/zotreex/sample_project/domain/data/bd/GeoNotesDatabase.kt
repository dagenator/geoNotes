package com.zotreex.sample_project.domain.data.bd

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zotreex.sample_project.domain.data.models.GeoNote

@Database(entities = [GeoNote::class], version = 1)
abstract class GeoNotesDatabase : RoomDatabase() {
    abstract fun getNoteDAO(): GeoNoteDAO
}