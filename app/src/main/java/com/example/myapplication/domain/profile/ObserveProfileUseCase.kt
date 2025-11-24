package com.example.myapplication.domain.profile

import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<Profile> = repository.profileFlow
}

