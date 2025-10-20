package com.example.myapplication.data

class NewsRepository(
    private val api: NewsApiService = RetrofitClient.api
) {
    suspend fun searchNews(
        query: String,
        language: String? = "ru",
        sortBy: String? = "publishedAt",
        page: Int = 1,
        pageSize: Int = 20
    ): List<Article> {
        val response = api.getEverything(
            query = query,
            language = language,
            sortBy = sortBy,
            page = page,
            pageSize = pageSize,
            apiKey = RetrofitClient.API_KEY
        )
        return response.articles.mapNotNull { it.toDomain() }
    }
}

private fun ArticleDto.toDomain(): Article? {
    val safeTitle = title ?: return null
    val safeUrl = url ?: return null
    return Article(
        id = safeUrl,
        title = safeTitle,
        description = description ?: "",
        imageUrl = urlToImage,
        sourceName = source?.name ?: "",
        publishedAt = publishedAt ?: "",
        author = author,
        url = safeUrl,
        content = content
    )
}


