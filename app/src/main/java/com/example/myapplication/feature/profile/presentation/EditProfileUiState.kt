package com.example.myapplication.feature.profile.presentation

data class EditProfileUiState(
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val favoritePairTime: String = "",
    val avatarUri: String = "",
    val timeError: String? = null,
    val isTimeValid: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveEnabled: Boolean = false
) {
    val showTimeError: Boolean get() = timeError != null
}

