package com.zotreex.sample_project.di

import android.content.Context
import androidx.room.Room
import com.zotreex.sample_project.MainActivity
import com.zotreex.sample_project.NoteSevice
import com.zotreex.sample_project.domain.api.YandexService
import com.zotreex.sample_project.domain.data.bd.GeoNotesDatabase
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(noteSevice: NoteSevice)


}

@Module(includes = [NetworkModule::class, AppBindModule::class, BDModule::class])
class AppModule(val context: Context) {

    @Provides
    fun provideContext(): Context {
        return context
    }

}

@Module
abstract class ViewModelModule {
//    @Binds
//    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@Module
interface AppBindModule {

}

@Module
class NetworkModule {
    @Provides
    fun provideYandexServiceApi(): YandexService {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://geocode-maps.yandex.ru/")
            .client(httpClient.build())
            .build()
        return retrofit.create()
    }
}

@Module
class BDModule {

    @Provides
    fun getNotesDatabase(context: Context): GeoNotesDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GeoNotesDatabase::class.java, "database-name"
        ).build()
    }

}
