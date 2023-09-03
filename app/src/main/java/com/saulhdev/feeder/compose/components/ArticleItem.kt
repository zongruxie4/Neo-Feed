package com.saulhdev.feeder.compose.components

import android.content.Intent
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.utils.RelativeTimeHelper
import kotlinx.coroutines.launch

@Composable
fun ArticleItem(
    article: FeedItem,
    repository: ArticleRepository,
    onClick: () -> Unit
) {
    val content = article.content
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Column {
            if (content.background_url.isNotEmpty()
                && !content.background_url.contains(".rss")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(content.background_url)
                        .crossfade(true)
                        .crossfade(500)
                        .build(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                )
            }

            Text(
                text = content.title,
                modifier = Modifier.padding(8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 5,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (content.text.isNotEmpty()) {
                Text(
                    text = Html.fromHtml(content.text, 0).toString(),
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 5,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(2f)
                ) {
                    Text(
                        text = content.source.title,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = RelativeTimeHelper.getDateFormattedRelative(
                            LocalContext.current,
                            (article.time / 1000) - 1000
                        ),
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    var bookmarked by remember { mutableStateOf(false) }

                    FavoriteButton(bookmarked = bookmarked) {
                        scope.launch {
                            repository.bookmarkArticle(article.id, !bookmarked)
                            bookmarked = !bookmarked
                        }
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    ShareButton {
                        val intent = Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, content.link)
                                putExtra(Intent.EXTRA_TITLE, content.title)
                                type = "text/plain"
                            },
                            null,
                        )
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
