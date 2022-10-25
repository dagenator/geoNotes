package com.zotreex.sample_project.domain.data.bd

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.zotreex.sample_project.domain.data.models.GeoNote

@Dao
interface GeoNoteDAO {
    @Query("SELECT * FROM GeoNote")
    fun getAll(): List<GeoNote>

    @Insert
    fun insert(vararg users: GeoNote)

    @Query("DELETE  FROM GeoNote where address = :address")
    fun delete(address:String)

}