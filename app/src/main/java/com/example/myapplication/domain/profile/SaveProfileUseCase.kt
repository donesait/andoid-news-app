package com.example.myapplication.domain.profile

class SaveProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: Profile) {
        repository.save(profile)
    }
}

