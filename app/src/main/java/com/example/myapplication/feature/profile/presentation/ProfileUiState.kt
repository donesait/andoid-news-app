package com.example.myapplication.feature.profile.presentation

import com.example.myapplication.domain.profile.Profile

data class ProfileUiState(
    val profile: Profile = Profile(),
    val isLoading: Boolean = true,
    val isResumeInProgress: Boolean = false
)

