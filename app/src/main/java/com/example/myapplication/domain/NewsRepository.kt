package com.example.myapplication.domain

import com.example.myapplication.core.AppResult

interface NewsRepository {
    suspend fun searchNews(
        query: String,
        language: String? = "ru",
        sortBy: String? = "publishedAt",
        page: Int = 1,
        pageSize: Int = 20
    ): AppResult<List<Article>>
}


