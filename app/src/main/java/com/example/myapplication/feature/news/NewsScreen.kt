package com.example.myapplication.feature.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.myapplication.domain.Article

@Composable
fun NewsScreen(vm: NewsViewModel = viewModel(), navController: NavController? = null) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.articles.isEmpty()) vm.refresh()
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            label = { Text(stringResource(id = com.example.myapplication.R.string.search_label)) },
            placeholder = { Text(stringResource(id = com.example.myapplication.R.string.search_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = {
                        navController?.navigateToDetails(article)
                    })
                }
                item {
                    if (!state.endReached && !state.isLoading) {
                        LaunchedEffect(state.articles.size) { vm.loadMore() }
                    }
                }
            }

            if (state.isLoading && state.articles.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error?.let { err ->
                AlertDialog(
                    onDismissRequest = { vm.dismissError() },
                    confirmButton = {
                        TextButton(onClick = { vm.dismissError(); vm.retry() }) {
                            Text(text = stringResource(id = com.example.myapplication.R.string.retry))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { vm.dismissError() }) {
                            Text(text = stringResource(id = com.example.myapplication.R.string.close))
                        }
                    },
                    title = { Text(text = stringResource(id = com.example.myapplication.R.string.error_title)) },
                    text = { Text(text = err) }
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(text = article.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (article.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = article.description, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Text(text = "Источник: ${article.sourceName}")
        }
    }
}


