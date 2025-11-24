package com.example.myapplication.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.domain.profile.ObserveProfileUseCase
import com.example.myapplication.feature.profile.data.ResumeDownloader
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    observeProfileUseCase: ObserveProfileUseCase,
    private val resumeDownloader: ResumeDownloader
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileViewEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeProfileUseCase().collect { profile ->
                _uiState.update { state -> state.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun onResumeClick() {
        val url = _uiState.value.profile.resumeUrl
        if (url.isBlank()) {
            viewModelScope.launch { _events.emit(ProfileViewEvent.ShowMessageRes(R.string.profile_resume_missing)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isResumeInProgress = true) }
            val result = resumeDownloader.downloadAndOpen(url)
            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message
                if (message.isNullOrBlank()) {
                    _events.emit(ProfileViewEvent.ShowMessageRes(R.string.profile_resume_error))
                } else {
                    _events.emit(ProfileViewEvent.ShowMessage(message))
                }
            }
            _uiState.update { it.copy(isResumeInProgress = false) }
        }
    }
}

sealed interface ProfileViewEvent {
    data class ShowMessage(val message: String) : ProfileViewEvent
    data class ShowMessageRes(val resId: Int) : ProfileViewEvent
}

