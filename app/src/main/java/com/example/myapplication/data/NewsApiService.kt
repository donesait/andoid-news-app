package com.example.myapplication.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("searchIn") searchIn: String? = null,
        @Query("sources") sources: String? = null,
        @Query("domains") domains: String? = null,
        @Query("excludeDomains") excludeDomains: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("language") language: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("pageSize") pageSize: Int? = 20,
        @Query("page") page: Int? = 1,
        @Query("apiKey") apiKey: String
    ): NewsResponseDto
}


