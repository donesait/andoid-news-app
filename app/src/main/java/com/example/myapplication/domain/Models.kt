package com.example.myapplication.domain

data class Article(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val sourceName: String,
    val publishedAt: String,
    val author: String?,
    val url: String,
    val content: String?
)


