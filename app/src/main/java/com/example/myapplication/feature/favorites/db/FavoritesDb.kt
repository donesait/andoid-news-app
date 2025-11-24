package com.example.myapplication.feature.favorites.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteArticleEntity::class], version = 1, exportSchema = false)
abstract class FavoritesDb : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        fun create(context: Context): FavoritesDb = Room.databaseBuilder(
            context,
            FavoritesDb::class.java,
            "favorites.db"
        ).fallbackToDestructiveMigration().build()
    }
}


