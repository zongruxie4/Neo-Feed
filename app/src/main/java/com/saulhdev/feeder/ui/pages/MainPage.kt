package com.saulhdev.feeder.ui.pages


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.saulhdev.feeder.ui.navigation.NavItem
import com.saulhdev.feeder.ui.navigation.NavigationSuiteScaffold
import com.saulhdev.feeder.ui.components.SlidePager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun MainPage(pageIndex: Int = 0) {
    val scope = rememberCoroutineScope()
    val pages = persistentListOf(
        NavItem.Feed,
        NavItem.Settings,
        NavItem.Sources,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }

    NavigationSuiteScaffold(
        pages = pages,
        currentState = currentPageIndex,
        onItemClick = { index ->
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    ) {
        SlidePager(
            modifier = Modifier
                .fillMaxSize(),
            pagerState = pagerState,
            pageItems = pages,
        )
    }
}