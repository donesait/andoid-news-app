package com.example.myapplication.feature.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.di.AppLocator
import com.example.myapplication.filters.NewsFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class FiltersUiState(
    val searchIn: String = "",
    val minDateIso: String = "",
    val language: String = "ru"
)

class FiltersViewModel : ViewModel() {
    private val filtersRepo = AppLocator.get().filtersRepository
    private val badgeCache = AppLocator.get().badgeCache

    private val _state = MutableStateFlow(FiltersUiState())
    val state: StateFlow<FiltersUiState> = _state.asStateFlow()

    init {
        filtersRepo.filtersFlow.onEach { f ->
            _state.value = FiltersUiState(
                searchIn = f.searchIn,
                minDateIso = f.minDateIso,
                language = f.language
            )
            badgeCache.setActive(f != NewsFilters())
        }.launchIn(viewModelScope)
    }

    fun updateSearchIn(value: String) { _state.value = _state.value.copy(searchIn = value) }
    fun updateMinDate(value: String) { _state.value = _state.value.copy(minDateIso = value) }
    fun updateLanguage(value: String) { _state.value = _state.value.copy(language = value) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            val filters = NewsFilters(s.searchIn.trim(), s.minDateIso.trim(), s.language.trim())
            filtersRepo.save(filters)
            badgeCache.setActive(filters != NewsFilters())
            onDone()
        }
    }

    fun reset(onDone: () -> Unit) {
        viewModelScope.launch {
            val defaults = NewsFilters()
            filtersRepo.save(defaults)
            badgeCache.setActive(false)
            _state.value = FiltersUiState()
            onDone()
        }
    }
}


