package com.dawidchmiel.bookshelfplanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: BookShelfViewModel, onBack: () -> Unit) {
    val query by viewModel.query.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val isRateLimitCooldownActive by viewModel.isRateLimitCooldownActive.collectAsState()

    val isLoading = searchState is SearchUiState.Loading
    val isGoEnabled = query.isNotBlank() && !isLoading && !isRateLimitCooldownActive

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search books") }, navigationIcon = { IconButton(onClick = onBack) { Text("‹") } }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("Title, author or topic") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (isGoEnabled) viewModel.search() })
                )
                Button(
                    onClick = viewModel::search,
                    enabled = isGoEnabled,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Go")
                }
            }

            when (val state = searchState) {
                SearchUiState.Idle -> Text("Search results will appear here.", modifier = Modifier.padding(top = 24.dp))
                SearchUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally))
                is SearchUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = viewModel::search,
                            enabled = !isLoading && !isRateLimitCooldownActive
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is SearchUiState.Success -> {
                    if (state.books.isEmpty()) {
                        Text("No books found.", modifier = Modifier.padding(top = 24.dp))
                    } else {
                        LazyColumn(
                             contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                             verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.books, key = { it.id }) { book ->
                                BookCard(book = book, actionText = "Add", onAction = { viewModel.save(book) })
                            }
                        }
                    }
                }
            }
        }
    }
}
