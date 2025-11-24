package com.example.myapplication.domain

import com.example.myapplication.core.AppResult

class SearchNewsUseCase(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        query: String,
        language: String? = "ru",
        sortBy: String? = "publishedAt",
        page: Int = 1,
        pageSize: Int = 20
    ): AppResult<List<Article>> = repository.searchNews(
        query = query,
        language = language,
        sortBy = sortBy,
        page = page,
        pageSize = pageSize
    )
}


