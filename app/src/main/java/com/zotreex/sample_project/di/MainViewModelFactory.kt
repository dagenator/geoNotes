package com.zotreex.sample_project.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zotreex.sample_project.domain.repository.YandexServiceRepository
import com.zotreex.sample_project.ui.MainViewModel
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory @Inject constructor(
    var yandexServiceRepository: YandexServiceRepository
) :
    ViewModelProvider.NewInstanceFactory() {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(yandexServiceRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }


}