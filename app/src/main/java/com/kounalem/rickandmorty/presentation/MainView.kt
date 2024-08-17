package com.kounalem.rickandmorty.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun MainView() {
    val viewModel = hiltViewModel<MainViewModel>()
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        is MainViewModel.State.Error -> Text(
            modifier = Modifier,
            text = state.message,
            color = MaterialTheme.colorScheme.error,
        )

        MainViewModel.State.Loading -> CircularProgressIndicator()
        is MainViewModel.State.Success -> {
            val swipeRefreshState =
                rememberSwipeRefreshState(
                    isRefreshing = state.isRefreshing,
                )
            val listState = rememberLazyListState()

            if (state.characters.isEmpty()) return

            SwipeRefresh(
                modifier = Modifier,
                state = swipeRefreshState,
                onRefresh = viewModel::refreshElements,
            ) {
                LazyColumn(state = listState) {
                    itemsIndexed(
                        items = state.characters,
                    ) { index, item ->
                        val isLastItem = index == (state.characters.size - 1)
                        val shouldLoadNextItems =
                            isLastItem && !state.endReached && !state.fetchingNewChars
                        if (shouldLoadNextItems) {
                            viewModel.loadNextItems()
                        }

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp), text = item
                        )
                    }

                    if (state.fetchingNewChars) {
                        item {
                            Row(
                                modifier =
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
