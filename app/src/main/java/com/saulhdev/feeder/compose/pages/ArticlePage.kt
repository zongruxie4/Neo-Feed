package com.saulhdev.feeder.compose.pages

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.RoundButton
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.components.WithBidiDeterminedLayoutDirection
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.compose.navigation.preferenceGraph
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.icon.phosphor.ArrowSquareOut
import com.saulhdev.feeder.icon.phosphor.ShareNetwork
import com.saulhdev.feeder.theme.LinkTextStyle
import com.saulhdev.feeder.utils.blobFile
import com.saulhdev.feeder.utils.blobFullFile
import com.saulhdev.feeder.utils.blobFullInputStream
import com.saulhdev.feeder.utils.blobInputStream
import com.saulhdev.feeder.utils.htmlFormattedText
import com.saulhdev.feeder.utils.launchView
import com.saulhdev.feeder.utils.shareIntent
import com.saulhdev.feeder.utils.unicodeWrap
import com.saulhdev.feeder.utils.urlEncode
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

@Composable
fun ArticleScreen(articleId: Long) {
    val context = LocalContext.current
    val repository = ArticleRepository(context)
    val article by repository.getArticleById(articleId).collectAsState(initial = null)
    val feed by repository.getFeedById(article?.feedId ?: 0).collectAsState(initial = null)

    val showFullArticle = repository.getFeedById(article?.feedId ?: 0)
        .collectAsState(initial = null).value?.fullTextByDefault ?: false

    val title = remember { mutableStateOf("Neo Feed") }
    val subTitle = remember { mutableStateOf("Neo Feed") }
    val feedTitle = remember { mutableStateOf("Neo Feed") }

    val navController = rememberNavController()
    val activity = (LocalContext.current as? Activity)
    BackHandler {
        if (navController.currentBackStackEntry?.destination?.route == null) {
            activity?.finish()
        } else {
            navController.popBackStack()
        }
    }
    title.value = article?.title ?: "Neo Feed"
    feedTitle.value = feed?.title ?: "Neo Feed"
    val currentUrl = article?.link ?: "Neo Feed"
    if (currentUrl != "Neo Feed") {
        subTitle.value = Uri.parse(currentUrl).host!!
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
        title = title.value,
        titleSize = 16.sp,
        subTitle = subTitle.value,
        showBackButton = true,
        actions = {
            RoundButton(
                icon = com.saulhdev.feeder.icon.Phosphor.ArrowSquareOut,
                description = stringResource(id = R.string.pref_browser_theme),
            ) {
                context.launchView(currentUrl)
            }
            RoundButton(
                icon = com.saulhdev.feeder.icon.Phosphor.ShareNetwork,
                description = stringResource(id = R.string.share),
            ) {
                context.shareIntent(currentUrl, title.value)
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
                    WithBidiDeterminedLayoutDirection(paragraph = title.value) {
                        Text(
                            text = title.value,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    WithBidiDeterminedLayoutDirection(paragraph = feedTitle.value) {
                        Text(
                            text = feedTitle.value,
                            style = MaterialTheme.typography.titleMedium.merge(LinkTextStyle()),
                            modifier = Modifier
                                .wrapContentWidth()
                                .clearAndSetSemantics {
                                    contentDescription = feedTitle.value
                                }
                                .clickable {
                                    MainActivity.createIntent(
                                        context,
                                        "${Routes.WEB_VIEW}/${article?.link?.urlEncode()}/"
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

fun NavGraphBuilder.articleGraph(route: String) {
    preferenceGraph(route, { }) { subRoute ->
        composable(
            route = subRoute("{id}"),
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0
            if (id != 0L) {
                ArticleScreen(articleId = id)
            }
        }
    }
}
