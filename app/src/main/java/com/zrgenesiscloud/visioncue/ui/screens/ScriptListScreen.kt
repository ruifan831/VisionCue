package com.zrgenesiscloud.visioncue.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zrgenesiscloud.visioncue.R
import com.zrgenesis.teleprompter.model.Script
import com.zrgenesis.teleprompter.repository.ScriptRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.zrgenesiscloud.visioncue.ui.components.PrimaryButton
import com.zrgenesiscloud.visioncue.ui.theme.Custom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptListScreen(
    scriptRepository: ScriptRepository,
    onScriptClick: (String) -> Unit,
    onNewScriptClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scripts = remember { mutableStateListOf<Script>() }
    val coroutineScope = rememberCoroutineScope()
    
    // State for search query
    var searchQuery by remember { mutableStateOf("") }
    
    // Load scripts from repository
    LaunchedEffect(Unit) {
        scriptRepository.getAllScripts().collect { scriptList ->
            scripts.clear()
            scripts.addAll(scriptList)
        }
    }

    // Filter scripts based on search query
    val filteredScripts = remember(searchQuery, scripts) {
        if (searchQuery.isEmpty()) {
            scripts
        } else {
            scripts.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.content.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.my_scripts)) },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d("TopAppBar", "Settings clicked")
                            onSettingsClick()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (scripts.isNotEmpty()) NewScriptButton(onClick = onNewScriptClick)
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        if (scripts.isEmpty()) {
            // Display the empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Custom.File,
                    contentDescription = stringResource(id = R.string.no_scripts),
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = stringResource(id = R.string.no_scripts_yet),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.create_first_script),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                NewScriptButton(onClick = onNewScriptClick)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search bar with functionality
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text(stringResource(id = R.string.search_scripts)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search_scripts)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.clear_search)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Show message when no results found
                if (filteredScripts.isEmpty() && searchQuery.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_search_results, searchQuery),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Show filtered scripts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredScripts) { script ->
                        ScriptCard(
                            script = script,
                            onClick = { onScriptClick(script.id) },
                            onEditClick = { onScriptClick(script.id) },
                            onDeleteClick = {
                                coroutineScope.launch {
                                    scriptRepository.deleteScript(script.id)
                                    scripts.removeIf { it.id == script.id }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScriptCard(
    script: Script,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = script.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = stringResource(id = R.string.last_edited, formatDate(script.updatedAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = script.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun NewScriptButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(id = R.string.create_new_script),
        onClick = onClick,
        leadingIcon = Icons.Default.Add
    )
}

private fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
} 