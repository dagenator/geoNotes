package com.zotreex.sample_project.domain.data.bd

import androidx.room.*
import com.zotreex.sample_project.domain.data.models.GeoNote

@Dao
interface GeoNoteDAO {
    @Query("SELECT * FROM GeoNote")
    fun getAll(): List<GeoNote>

    @Insert
    fun insert(vararg users: GeoNote)

    @Query("DELETE  FROM GeoNote where address = :address")
    fun delete(address:String)

    @Query("SELECT *  FROM GeoNote where address = :address")
    fun getByAddress(address:String):GeoNote?

    @Update
    fun update(vararg users: GeoNote)

}