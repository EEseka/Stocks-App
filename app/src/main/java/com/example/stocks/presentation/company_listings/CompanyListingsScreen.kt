package com.example.stocks.presentation.company_listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CompanyListingsScreen(
    viewModel: CompanyListingsViewModel = hiltViewModel(),
    onCompanyClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val swipeRefreshState =
        rememberPullRefreshState(refreshing = state.isRefreshing, onRefresh = {
            viewModel.onEvent(CompanyListingsEvent.Refresh)
        })

    // FocusRequester to request focus control
    val focusRequester = remember { FocusRequester() }

    // FocusManager to clear the focus
    val focusManager = LocalFocusManager.current

    // KeyboardController to control the software keyboard
    val keyboardController = LocalSoftwareKeyboardController.current

    if (state.isLoading && state.companies.isEmpty()) {
        // Show a full-screen loading indicator for the initial load
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Once initial load is done, use SwipeRefresh for refreshes

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(swipeRefreshState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { newValue ->
                            viewModel.onEvent(
                                CompanyListingsEvent.OnSearchQueryChange(newValue)
                            )
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        shape = MaterialTheme.shapes.medium,
                        placeholder = {
                            Text(text = "Search...")
                        },
                        maxLines = 1,
                        singleLine = true,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        ),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    viewModel.onEvent(CompanyListingsEvent.OnSearchQueryChange(""))
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search"
                                    )
                                }
                            }
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.isLoading && !state.isRefreshing) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        } else if (state.error != null) {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = state.error!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(onClick = {
                                        viewModel.onEvent(CompanyListingsEvent.Refresh)
                                    }) {
                                        Text(text = "Retry")
                                    }
                                }
                            }
                        } else if (state.companies.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No companies found",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                            }
                        } else {
                            items(state.companies.size) { i ->
                                val companies = state.companies
                                CompanyItem(
                                    company = companies[i],
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCompanyClick(companies[i].symbol)
                                        }
                                        .padding(16.dp)
                                )
                                if (i < state.companies.size) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = state.isRefreshing,
                    state = swipeRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}