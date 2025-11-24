package com.example.myapplication.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.profile.ObserveProfileUseCase
import com.example.myapplication.domain.profile.Profile
import com.example.myapplication.domain.profile.SaveProfileUseCase
import com.example.myapplication.feature.profile.reminder.ProfileReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class EditProfileViewModel(
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val reminderScheduler: ProfileReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val profile = observeProfileUseCase().first()
            _uiState.update { state ->
                state.copy(
                    fullName = profile.fullName,
                    position = profile.position,
                    resumeUrl = profile.resumeUrl,
                    favoritePairTime = profile.favoritePairTime,
                    avatarUri = profile.avatarUri
                ).validated()
            }
        }
    }

    fun onFullNameChange(value: String) = updateState { it.copy(fullName = value).validated() }

    fun onPositionChange(value: String) = updateState { it.copy(position = value).validated() }

    fun onResumeUrlChange(value: String) = updateState { it.copy(resumeUrl = value).validated() }

    fun onFavoriteTimeChange(value: String) = updateState { it.copy(favoritePairTime = value).validated() }

    fun onAvatarChange(uri: String) = updateState { it.copy(avatarUri = uri).validated() }

    fun saveProfile() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled || currentState.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val profile = _uiState.value.toProfile()
            runCatching {
                saveProfileUseCase(profile)
                if (profile.favoritePairTime.isNotBlank()) {
                    reminderScheduler.schedule(profile.favoritePairTime, profile.fullName)
                } else {
                    reminderScheduler.cancel()
                }
            }.onSuccess {
                _events.emit(EditProfileEvent.Saved)
            }.onFailure { error ->
                _events.emit(EditProfileEvent.ShowMessage(error.message ?: "Не удалось сохранить профиль"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun updateState(transform: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.update(transform)
    }

    private fun EditProfileUiState.validated(): EditProfileUiState {
        val timeValid = favoritePairTime.isBlank() || TIME_REGEX.matches(favoritePairTime)
        val error = if (timeValid || favoritePairTime.isBlank()) null else TIME_ERROR
        val saveEnabled = fullName.isNotBlank() && resumeUrl.isNotBlank() && timeValid
        return copy(timeError = error, isTimeValid = timeValid, isSaveEnabled = saveEnabled)
    }

    private fun EditProfileUiState.toProfile() = Profile(
        fullName = fullName.trim(),
        position = position.trim(),
        resumeUrl = resumeUrl.trim(),
        favoritePairTime = favoritePairTime.trim(),
        avatarUri = avatarUri
    )

    companion object {
        private val TIME_REGEX = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d\$")
        private const val TIME_ERROR = "Введите время в формате HH:mm"
    }
}

sealed interface EditProfileEvent {
    data object Saved : EditProfileEvent
    data class ShowMessage(val message: String) : EditProfileEvent
}

