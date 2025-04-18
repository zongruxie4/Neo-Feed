package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.saulhdev.feeder.compose.components.SlidePager
import com.saulhdev.feeder.compose.navigation.NavItem
import com.saulhdev.feeder.compose.navigation.NeoNavigationSuiteScaffold
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun MainPage(pageIndex: Int = 0) {
    val scope = rememberCoroutineScope()
    val pages = persistentListOf(
        NavItem.Overlay,
        NavItem.Settings,
        NavItem.Sources,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }

    NeoNavigationSuiteScaffold(
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
                //.padding(paddingValues)
                .fillMaxSize(),
            pagerState = pagerState,
            pageItems = pages,
        )
    }
}