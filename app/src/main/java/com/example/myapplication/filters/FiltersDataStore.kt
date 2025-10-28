package com.example.myapplication.filters

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "news_filters")

class FiltersRepository(private val context: Context) {
    private object Keys {
        val SEARCH_IN = stringPreferencesKey("search_in")
        val MIN_DATE = stringPreferencesKey("min_date")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val filtersFlow: Flow<NewsFilters> = context.dataStore.data.map { prefs: Preferences ->
        NewsFilters(
            searchIn = prefs[Keys.SEARCH_IN].orEmpty(),
            minDateIso = prefs[Keys.MIN_DATE].orEmpty(),
            language = prefs[Keys.LANGUAGE] ?: "ru"
        )
    }

    suspend fun save(filters: NewsFilters) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SEARCH_IN] = filters.searchIn
            prefs[Keys.MIN_DATE] = filters.minDateIso
            prefs[Keys.LANGUAGE] = filters.language
        }
    }
}


