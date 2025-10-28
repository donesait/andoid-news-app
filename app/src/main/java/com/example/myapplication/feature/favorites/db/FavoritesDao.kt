package com.example.myapplication.feature.favorites.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_articles ORDER BY publishedAt DESC")
    fun observeAll(): Flow<List<FavoriteArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteArticleEntity)

    @Delete
    suspend fun delete(entity: FavoriteArticleEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_articles WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean
}


