package com.example.myapplication.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.domain.Article

@Composable
fun FavoritesScreen(vm: FavoritesViewModel = viewModel()) {
    val items by vm.favorites.collectAsState()
    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Здесь пока пусто")
            Spacer(Modifier.height(8.dp))
            Text("Добавляйте новости в избранное из списка")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { article ->
                FavoriteCard(article, onRemove = { vm.toggle(article) })
            }
        }
    }
}

@Composable
private fun FavoriteCard(article: Article, onRemove: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            AsyncImage(model = article.imageUrl, contentDescription = article.title, modifier = Modifier.fillMaxWidth().height(160.dp))
            Spacer(Modifier.height(8.dp))
            Text(article.title)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRemove) { Text("Удалить из избранного") }
        }
    }
}


