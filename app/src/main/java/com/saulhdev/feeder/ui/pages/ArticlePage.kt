package com.saulhdev.feeder.ui.pages

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.ui.components.RoundButton
import com.saulhdev.feeder.ui.components.ViewWithActionBar
import com.saulhdev.feeder.ui.components.WithBidiDeterminedLayoutDirection
import com.saulhdev.feeder.ui.compose.icon.Phosphor
import com.saulhdev.feeder.ui.compose.icon.phosphor.ArrowSquareOut
import com.saulhdev.feeder.ui.compose.icon.phosphor.ShareNetwork
import com.saulhdev.feeder.ui.compose.theme.LinkTextStyle
import com.saulhdev.feeder.ui.navigation.Routes
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobFullFile
import com.saulhdev.feeder.utils.blobFullInputStream
import com.saulhdev.feeder.utils.blobInputStream
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.utils.extensions.launchView
import com.saulhdev.feeder.utils.extensions.shareIntent
import com.saulhdev.feeder.utils.htmlFormattedText
import com.saulhdev.feeder.utils.unicodeWrap
import com.saulhdev.feeder.utils.urlEncode
import com.saulhdev.feeder.viewmodels.ArticleViewModel
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

@Composable
fun ArticleScreen(
    articleId: Long,
    viewModel: ArticleViewModel = koinNeoViewModel(),
    onDismiss: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val article by viewModel.articleById(articleId).collectAsState(initial = null)
    val feed by viewModel.getFeedById(article?.feedId ?: 0).collectAsState(initial = null)

    val showFullArticle by remember {
        derivedStateOf { feed?.fullTextByDefault ?: false }
    }

    val title by remember { derivedStateOf { article?.title ?: "Neo Feed" } }
    val currentUrl by remember { derivedStateOf { article?.link ?: "Neo Feed" } }
    val subTitle by remember {
        derivedStateOf {
            (if (currentUrl != "Neo Feed") Uri.parse(currentUrl).host else null)
                ?: feed?.title
                ?: "Neo Feed"
        }
    }
    val feedTitle by remember { derivedStateOf { feed?.title ?: "Neo Feed" } }

    val navController = rememberNavController()
    BackHandler {
        onDismiss?.invoke()
            ?: if (navController.currentBackStackEntry?.destination?.route == null) {
                activity?.finish()
            } else {
                navController.popBackStack()
            }
    }

    val dateTimeFormat: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())

    val authorDate = when {
        article?.author == null && article?.pubDate != null ->
            stringResource(
                R.string.on_date,
                (article?.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat)
            )

        article?.author != null && article?.pubDate != null ->
            stringResource(
                R.string.by_author_on_date,
                // Must wrap author in unicode marks to ensure it formats
                // correctly in RTL
                context.unicodeWrap(article?.author ?: ""),
                (article?.pubDate ?: ZonedDateTime.now()).format(dateTimeFormat)
            )

        else                                                -> null
    }

    ViewWithActionBar(
        title = title,
        titleSize = 16.sp,
        subTitle = subTitle,
        showBackButton = true,
        actions = {
            RoundButton(
                icon = Phosphor.ArrowSquareOut,
                description = stringResource(id = R.string.pref_browser_theme),
            ) {
                context.launchView(currentUrl)
            }
            RoundButton(
                icon = Phosphor.ShareNetwork,
                description = stringResource(id = R.string.share),
            ) {
                context.shareIntent(currentUrl, title)
            }
        }

    ) { paddingValues ->
        SelectionContainer {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = 4.dp,
                        end = 4.dp,
                        bottom = paddingValues.calculateBottomPadding() + 8.dp
                    ),
            ) {
                item {
                    WithBidiDeterminedLayoutDirection(paragraph = title) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    WithBidiDeterminedLayoutDirection(paragraph = feedTitle) {
                        Text(
                            text = feedTitle,
                            style = MaterialTheme.typography.titleMedium.merge(LinkTextStyle()),
                            modifier = Modifier
                                .wrapContentWidth()
                                .clearAndSetSemantics {
                                    contentDescription = feedTitle
                                }
                                .clickable {
                                    MainActivity.navigateIntent(
                                        context,
                                        "${Routes.WEB_VIEW}/${article?.link?.urlEncode()}"
                                    )
                                }
                        )
                    }

                    if (authorDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        WithBidiDeterminedLayoutDirection(paragraph = authorDate) {
                            Text(
                                text = authorDate,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showFullArticle) {
                    if (blobFullFile(articleId, context.filesDir).isFile) {
                        blobFullInputStream(articleId, context.filesDir).use {
                            htmlFormattedText(
                                inputStream = it,
                                baseUrl = article?.link ?: "",
                                imagePlaceholder = R.drawable.placeholder_image_article_day,
                                onLinkClick = context::launchView
                            )
                        }
                    } else {
                        item {
                            NotFoundView()
                        }
                    }
                } else {
                    if (blobFile(articleId, context.filesDir).isFile) {
                        blobInputStream(articleId, context.filesDir).use {
                            htmlFormattedText(
                                inputStream = it,
                                baseUrl = article?.link ?: "",
                                imagePlaceholder = R.drawable.placeholder_image_article_day,
                                onLinkClick = context::launchView
                            )
                        }
                    } else {
                        item {
                            NotFoundView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotFoundView() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(id = R.string.article_not_found))
    }
}