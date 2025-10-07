package com.example.myapplication.feature.news

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.myapplication.data.Article
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

const val DETAILS_ROUTE = "news/details/{articleJson}"

fun NavGraphBuilder.newsGraph(navController: NavController) {
    composable("news") { NewsScreen(navController = navController) }
    composable(DETAILS_ROUTE) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("articleJson").orEmpty()
        val json = URLDecoder.decode(encoded, Charsets.UTF_8)
        val article = Gson().fromJson(json, Article::class.java)
        NewsDetailsScreen(article)
    }
}

fun NavController.navigateToDetails(article: Article) {
    val json = Gson().toJson(article)
    val encoded = URLEncoder.encode(json, Charsets.UTF_8)
    navigate("news/details/$encoded")
}


