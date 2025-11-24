package com.example.myapplication.feature.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.Article
import com.example.myapplication.feature.favorites.db.FavoritesDb
import com.example.myapplication.di.AppLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: FavoritesRepository = AppLocator.get().favoritesRepository

    val favorites: StateFlow<List<Article>> = repo.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggle(article: Article) {
        viewModelScope.launch { repo.toggle(article) }
    }
}


