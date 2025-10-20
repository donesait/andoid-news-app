@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.feature.news.NewsScreen
import com.example.myapplication.feature.news.newsGraph
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppScaffold()
            }
        }
    }
}

private enum class RootDestinations(val route: String, val label: String) {
    NEWS("news", "Новости"),
    FAVORITES("favorites", "Избранное"),
    SETTINGS("settings", "Настройки")
}

@Composable
private fun AppScaffold() {
    val navController = rememberNavController()
    val destinations = listOf(
        RootDestinations.NEWS,
        RootDestinations.FAVORITES,
        RootDestinations.SETTINGS
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route ?: RootDestinations.NEWS.route
            val isDetails = currentRoute.startsWith("news/details")
            val title = when {
                isDetails -> "Детали"
                currentRoute == RootDestinations.NEWS.route -> RootDestinations.NEWS.label
                currentRoute == RootDestinations.FAVORITES.route -> RootDestinations.FAVORITES.label
                currentRoute == RootDestinations.SETTINGS.route -> RootDestinations.SETTINGS.label
                else -> ""
            }
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (isDetails) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (dest) {
                                RootDestinations.NEWS -> Icon(Icons.Default.Home, contentDescription = null)
                                RootDestinations.FAVORITES -> Icon(Icons.Default.Bookmark, contentDescription = null)
                                RootDestinations.SETTINGS -> Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RootDestinations.NEWS.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            newsGraph(navController) // Используем newsGraph вместо прямого composable для news
            composable(RootDestinations.FAVORITES.route) { Text("Избранное (скоро)") }
            composable(RootDestinations.SETTINGS.route) { Text("Настройки (скоро)") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        AppScaffold()
    }
}