package com.example.myapplication.feature.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.example.myapplication.domain.Article

@Composable
fun NewsDetailsScreen(article: Article) {
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val (imageRef, titleRef, metaRef, contentRef) = createRefs()

        AsyncImage(
            model = article.imageUrl,
            contentDescription = article.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .constrainAs(imageRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = article.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(imageRef.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
        )

        Text(
            text = "Источник: ${article.sourceName} • ${article.publishedAt}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.constrainAs(metaRef) {
                top.linkTo(titleRef.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            }
        )

        ElevatedCard(
            modifier = Modifier.constrainAs(contentRef) {
                top.linkTo(metaRef.bottom, margin = 12.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                height = androidx.constraintlayout.compose.Dimension.preferredWrapContent
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (article.description.isNotBlank()) {
                    Text(text = article.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                article.content?.let { Text(text = it, style = MaterialTheme.typography.bodyLarge) }
            }
        }
    }
}


