package com.example.myapplication.filters

data class NewsFilters(
    val searchIn: String = "",
    val minDateIso: String = "",
    val language: String = "ru"
)


