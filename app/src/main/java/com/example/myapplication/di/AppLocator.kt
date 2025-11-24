package com.example.myapplication.di

import android.app.Application
import android.content.Context
import com.example.myapplication.filters.FiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.myapplication.feature.favorites.db.FavoritesDb
import com.example.myapplication.feature.favorites.FavoritesRepository
import com.example.myapplication.domain.profile.ProfileRepository
import com.example.myapplication.domain.profile.ObserveProfileUseCase
import com.example.myapplication.domain.profile.SaveProfileUseCase
import com.example.myapplication.feature.profile.data.ProfileRepositoryImpl
import com.example.myapplication.feature.profile.data.ResumeDownloader
import com.example.myapplication.feature.profile.presentation.EditProfileViewModelFactory
import com.example.myapplication.feature.profile.presentation.ProfileViewModelFactory
import com.example.myapplication.feature.profile.reminder.ProfileReminderScheduler

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
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(app)
    private val observeProfileUseCase = ObserveProfileUseCase(profileRepository)
    private val saveProfileUseCase = SaveProfileUseCase(profileRepository)
    private val resumeDownloader = ResumeDownloader(app)
    private val reminderScheduler = ProfileReminderScheduler(app)

    fun profileViewModelFactory(): ProfileViewModelFactory =
        ProfileViewModelFactory(observeProfileUseCase, resumeDownloader)

    fun editProfileViewModelFactory(): EditProfileViewModelFactory =
        EditProfileViewModelFactory(observeProfileUseCase, saveProfileUseCase, reminderScheduler)

    companion object {
        @Volatile private var instance: AppLocator? = null
        fun init(app: Application) {
            if (instance == null) instance = AppLocator(app)
        }
        fun get(): AppLocator = checkNotNull(instance)
    }
}


