package com.example.myapplication.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.domain.profile.ObserveProfileUseCase
import com.example.myapplication.domain.profile.SaveProfileUseCase
import com.example.myapplication.feature.profile.data.ResumeDownloader
import com.example.myapplication.feature.profile.reminder.ProfileReminderScheduler

class ProfileViewModelFactory(
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val resumeDownloader: ResumeDownloader
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(observeProfileUseCase, resumeDownloader) as T
    }
}

class EditProfileViewModelFactory(
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val reminderScheduler: ProfileReminderScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditProfileViewModel(observeProfileUseCase, saveProfileUseCase, reminderScheduler) as T
    }
}

