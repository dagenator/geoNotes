package com.zotreex.sample_project.domain.api

import com.zotreex.sample_project.domain.data.models.Geocode
import retrofit2.http.GET
import retrofit2.http.Query

interface YandexService {
    //todo ОБЕРНУТЬ в НЕКИЙ ApiResult с перехватом try catch
//    @GET("sampleDir")
//    suspend fun getSampleList(): List<SampleItem>

    @GET("/1.x/")
    suspend fun getGeocode(
        @Query("apikey") apikey: String,
        @Query("format") format: String,
        @Query("geocode") geocode: String
    ): Geocode
}