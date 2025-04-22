package com.saulhdev.feeder.compose.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ActionButton
import com.saulhdev.feeder.compose.components.ChipsSwitch
import com.saulhdev.feeder.compose.components.DeSelectAll
import com.saulhdev.feeder.compose.components.ExpandableItemsBlock
import com.saulhdev.feeder.compose.components.SelectChip
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.ArrowUUpLeft
import com.saulhdev.feeder.icon.phosphor.Check
import com.saulhdev.feeder.icon.phosphor.SortAscending
import com.saulhdev.feeder.icon.phosphor.SortDescending
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.utils.extensions.koinNeoViewModel
import com.saulhdev.feeder.viewmodel.ArticlesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.compose.koinInject


@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SortFilterSheet(
    viewModel: ArticlesViewModel = koinNeoViewModel(),
    prefs: FeedPreferences = koinInject(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val activeSources by viewModel.activeFeeds.collectAsState()
    val sortFilterModel by viewModel.prefSortFilter.collectAsState()

    var sortPrefVar by prefs.sortingFilter
    var sortAscPrefVar by prefs.sortingAsc
    var sourcesPrefVar by prefs.sourcesFilter

    var sortOption by remember(sortFilterModel.sort) {
        mutableStateOf(sortFilterModel.sort)
    }
    var sortAscOption by remember(sortFilterModel.sortAsc) {
        mutableStateOf(sortFilterModel.sortAsc)
    }
    val sourcesOption = remember(sortFilterModel.sourcesFilter) {
        mutableStateListOf(*sortFilterModel.sourcesFilter.toTypedArray())
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.action_reset),
                        icon = Phosphor.ArrowUUpLeft,
                        positive = false
                    ) {
                        sortPrefVar = prefs.sortingFilter.defaultValue
                        sortAscPrefVar = prefs.sortingAsc.defaultValue
                        sourcesPrefVar = prefs.sourcesFilter.defaultValue
                        onDismiss()
                    }
                    ActionButton(
                        text = stringResource(id = R.string.action_apply),
                        icon = Phosphor.Check,
                        modifier = Modifier.weight(1f),
                        positive = true,
                        onClick = {
                            sortPrefVar = sortOption
                            sortAscPrefVar = sortAscOption
                            sourcesPrefVar = sourcesOption.toSet()
                            onDismiss()
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                )
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.sorting_order),
                    preExpanded = true,
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        prefs.sortingFilter.entries.forEach {
                            SelectChip(
                                text = it.value,
                                checked = it.key == sortOption,
                                alwaysShowIcon = false,
                            ) {
                                sortOption = it.key
                            }
                        }
                    }
                    ChipsSwitch(
                        firstTextId = R.string.sort_ascending,
                        firstIcon = Phosphor.SortAscending,
                        secondTextId = R.string.sort_descending,
                        secondIcon = Phosphor.SortDescending,
                        firstSelected = sortAscOption,
                        onCheckedChange = { checked ->
                            sortAscOption = checked
                        }
                    )
                }
            }
            item {
                ExpandableItemsBlock(
                    heading = stringResource(id = R.string.title_sources),
                    preExpanded = sourcesOption.isNotEmpty(),
                ) {
                    DeSelectAll(activeSources.map { it.id.toString() }, sourcesOption)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        activeSources.sortedBy { it.title.lowercase() }.forEach {
                            val checked by remember(sourcesOption.toString()) {
                                mutableStateOf(!sourcesOption.contains(it.id.toString()))
                            }

                            SelectChip(
                                text = it.title,
                                checked = checked,
                            ) {
                                if (checked) sourcesOption.add(it.id.toString())
                                else sourcesOption.remove(it.id.toString())
                            }
                        }
                    }
                }
            }
            // TODO add categories/tags filter
        }
    }
}
