package com.example.myapplication.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.AppResult
import com.example.myapplication.domain.Article
import com.example.myapplication.domain.SearchNewsUseCase
import com.example.myapplication.data.NewsRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val articles: List<Article> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val endReached: Boolean = false
)

class NewsViewModel(
    private val searchNews: SearchNewsUseCase = SearchNewsUseCase(NewsRepositoryImpl())
) : ViewModel() {
    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
        searchDebounced()
    }

    fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    fun retry() {
        searchJob?.cancel()
        load(reset = _state.value.page == 1 || _state.value.articles.isEmpty())
    }

    fun refresh() {
        searchJob?.cancel()
        _state.value = _state.value.copy(page = 1, endReached = false)
        load(reset = true)
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.endReached) return
        _state.value = s.copy(page = s.page + 1)
        load(reset = false)
    }

    private fun searchDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            refresh()
        }
    }

    private fun load(reset: Boolean) {
        val current = _state.value
        val q = current.query.ifBlank { "технологии" }
        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            when (val result = searchNews(
                query = q,
                page = current.page,
                pageSize = 20,
                sortBy = "publishedAt",
                language = "ru"
            )) {
                is AppResult.Success -> {
                    val items = result.data
                    val merged = if (reset) items else current.articles + items
                    _state.value = _state.value.copy(
                        isLoading = false,
                        articles = merged,
                        endReached = items.isEmpty()
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}


