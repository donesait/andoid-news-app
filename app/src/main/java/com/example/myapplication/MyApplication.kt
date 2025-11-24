package com.example.myapplication

import android.app.Application
import com.example.myapplication.di.AppLocator
import com.example.myapplication.filters.NewsFilters
import kotlinx.coroutines.runBlocking

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocator.init(this)
        // Сбрасываем фильтры при каждом запуске приложения
        runBlocking {
            AppLocator.get().filtersRepository.save(NewsFilters())
            AppLocator.get().badgeCache.setActive(false)
        }
    }
}


