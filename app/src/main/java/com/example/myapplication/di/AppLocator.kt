package com.example.myapplication.di

import android.app.Application
import android.content.Context
import com.example.myapplication.filters.FiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.myapplication.feature.favorites.db.FavoritesDb
import com.example.myapplication.feature.favorites.FavoritesRepository

class BadgeCache {
    private val _hasActiveFilters = MutableStateFlow(false)
    val hasActiveFilters: StateFlow<Boolean> = _hasActiveFilters
    fun setActive(active: Boolean) { _hasActiveFilters.value = active }
}

class AppLocator private constructor(app: Application) {
    val badgeCache = BadgeCache()
    val filtersRepository = FiltersRepository(app)
    private val favoritesDb: FavoritesDb = FavoritesDb.create(app)
    val favoritesRepository: FavoritesRepository = FavoritesRepository(favoritesDb.favoritesDao())

    companion object {
        @Volatile private var instance: AppLocator? = null
        fun init(app: Application) {
            if (instance == null) instance = AppLocator(app)
        }
        fun get(): AppLocator = checkNotNull(instance)
    }
}


