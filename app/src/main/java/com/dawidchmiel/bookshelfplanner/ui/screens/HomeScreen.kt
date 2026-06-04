package com.dawidchmiel.bookshelfplanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dawidchmiel.bookshelfplanner.model.BookStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BookShelfViewModel,
    onSearchClick: () -> Unit,
    onBookClick: (String) -> Unit
) {
    val books by viewModel.savedBooks.collectAsState()
    var selectedStatus by remember { mutableStateOf<BookStatus?>(null) }
    val visibleBooks = selectedStatus?.let { status -> books.filter { it.status == status } } ?: books

    Scaffold(
        topBar = { TopAppBar(title = { Text("BookShelf Planner") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Your reading plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onSearchClick) { Text("Search") }
            }

            if (books.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Reading Stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total books: ${books.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("Reading: ${books.count { it.status == BookStatus.READING }}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text("Finished: ${books.count { it.status == BookStatus.FINISHED }}", style = MaterialTheme.typography.bodyMedium)
                                val finishedPages = books.filter { it.status == BookStatus.FINISHED }.sumOf { it.pageCount ?: 0 }
                                Text("Pages read: $finishedPages", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    FilterChip(selected = selectedStatus == null, onClick = { selectedStatus = null }, label = { Text("All") })
                    BookStatus.entries.forEach { status ->
                        FilterChip(selected = selectedStatus == status, onClick = { selectedStatus = status }, label = { Text(status.label) })
                    }
                }

                val sortOrder by viewModel.sortOrder.collectAsState()
                var expanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Sort books")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        BookSortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label) },
                                onClick = {
                                    viewModel.onSortOrderChange(order)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (visibleBooks.isEmpty()) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (books.isEmpty()) "No books yet. Search Google Books and add your first item." else "No books in this filter.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.weight(1f))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleBooks, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            actionText = "Remove",
                            onAction = { viewModel.delete(book) },
                            onClick = { onBookClick(book.id) }
                        )
                    }
                }
            }
        }
    }
}
