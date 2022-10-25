package com.zotreex.sample_project.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zotreex.sample_project.domain.data.models.GeoNote
import com.zotreex.sample_project.domain.data.models.Geocode
import com.zotreex.sample_project.domain.data.models.Resource
import com.zotreex.sample_project.domain.repository.YandexServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val yandexServiceRepository: YandexServiceRepository
) : ViewModel() {

    var geocodeLiveData = MutableLiveData<Resource<Geocode>>()
    var geoNotes = MutableLiveData<List<GeoNote>>()


    fun getGeocode(lat: Double, lng: Double) {
        viewModelScope.launch {
            yandexServiceRepository.getGeocode(lat, lng).collect {
                geocodeLiveData.value = it
            }
        }
    }

    fun getNotes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                geoNotes.postValue( yandexServiceRepository.getAllNotes())
            }
        }
    }

    fun insertNote(address: String, lat: Double, long: Double, note: String){
        viewModelScope.launch {
            yandexServiceRepository.saveNewNote(address, lat, long, note)
        }
    }

    fun updateNote(address: String, lat: Double, long: Double, note: String){
        viewModelScope.launch {
            yandexServiceRepository.updateNewNote(address, lat, long, note)
        }
    }

    fun deleteNote(address: String){
        viewModelScope.launch {
            yandexServiceRepository.deleteNote(address)
        }
    }



}