package com.example.myapplication.domain.profile

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profileFlow: Flow<Profile>
    suspend fun save(profile: Profile)
}

