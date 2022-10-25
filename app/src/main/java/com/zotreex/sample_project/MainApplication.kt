package com.zotreex.sample_project

import android.app.Application
import android.content.Context
import com.yandex.mapkit.MapKitFactory
import com.zotreex.sample_project.di.AppComponent
import com.zotreex.sample_project.di.AppModule
import com.zotreex.sample_project.di.DaggerAppComponent

class MainApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(context = this))
            .build()
        MapKitFactory.setApiKey("72012831-245a-4b00-b20a-8da784a3c595")
    }
}