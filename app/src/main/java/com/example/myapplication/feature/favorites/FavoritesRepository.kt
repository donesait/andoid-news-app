package com.example.myapplication.feature.favorites

import com.example.myapplication.domain.Article
import com.example.myapplication.feature.favorites.db.FavoriteArticleEntity
import com.example.myapplication.feature.favorites.db.FavoritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(private val dao: FavoritesDao) {
    fun observeFavorites(): Flow<List<Article>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun toggle(article: Article) {
        val exists = dao.isFavorite(article.id)
        if (exists) dao.delete(article.toEntity()) else dao.upsert(article.toEntity())
    }
}

private fun FavoriteArticleEntity.toDomain() = Article(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    sourceName = sourceName,
    publishedAt = publishedAt,
    author = author,
    url = url,
    content = content
)

private fun Article.toEntity() = FavoriteArticleEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    sourceName = sourceName,
    publishedAt = publishedAt,
    author = author,
    url = url,
    content = content
)


