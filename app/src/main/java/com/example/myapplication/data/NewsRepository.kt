package com.example.myapplication.data

import com.example.myapplication.core.AppResult
import com.example.myapplication.core.DefaultDispatcherProvider
import com.example.myapplication.core.DispatcherProvider
import com.example.myapplication.domain.Article
import com.example.myapplication.domain.NewsRepository
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class NewsRepositoryImpl(
    private val api: NewsApiService = RetrofitClient.api,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : NewsRepository {
    override suspend fun searchNews(
        query: String,
        language: String?,
        sortBy: String?,
        page: Int,
        pageSize: Int
    ): AppResult<List<Article>> = withContext(dispatchers.io) {
        try {
            val response = api.getEverything(
                query = query,
                language = language,
                sortBy = sortBy,
                page = page,
                pageSize = pageSize,
                apiKey = RetrofitClient.API_KEY
            )
            val items = response.articles.mapNotNull { it.toDomain() }
            AppResult.Success(items)
        } catch (e: IOException) {
            AppResult.Error("Проблемы с сетью. Проверьте соединение.", e)
        } catch (e: HttpException) {
            AppResult.Error("Ошибка сервера: ${e.code()}", e)
        } catch (e: Throwable) {
            AppResult.Error("Неизвестная ошибка", e)
        }
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

