package com.example.a7_1p.ui.screens

import com.example.a7_1p.BuildConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a7_1p.R
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    onCreatePostClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val databaseHelper = remember { LostFoundDatabaseHelper(context) }
    val items = remember { mutableStateListOf<com.example.a7_1p.data.LostFoundItem>() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var controlsExpanded by remember { mutableStateOf(false) }

    val sampleImageUri = "android.resource://${context.packageName}/${R.drawable.ic_launcher_foreground}"

    val quickLostItem = LostFoundItem(
        type = "Lost",
        name = "Black Wallet",
        phone = "0400000001",
        description = "Lost near library with student card inside.",
        createdAtMillis = System.currentTimeMillis(),
        location = "Campus Library",
        latitude = -37.8401,
        longitude = 144.9467,
        category = "Wallets",
        imageUri = sampleImageUri
    )

    val quickFoundItem = LostFoundItem(
        type = "Found",
        name = "Silver Keys",
        phone = "0400000002",
        description = "Found set of two keys at cafeteria counter.",
        createdAtMillis = System.currentTimeMillis() - 60_000,
        location = "Campus Cafeteria",
        latitude = -37.8410,
        longitude = 144.9451,
        category = "Keys",
        imageUri = "placeholder-image-path"
    )

    val quickBulkItems = listOf(
        LostFoundItem(
            type = "Lost",
            name = "Grey Backpack",
            phone = "0400000003",
            description = "Contains notebooks and a charger.",
            createdAtMillis = System.currentTimeMillis() - 2 * 60_000,
            location = "Engineering Building",
            latitude = -37.8425,
            longitude = 144.9443,
            category = "Other",
            imageUri = sampleImageUri
        ),
        LostFoundItem(
            type = "Found",
            name = "Bluetooth Earbuds",
            phone = "0400000004",
            description = "Found in lecture hall row C.",
            createdAtMillis = System.currentTimeMillis() - 3 * 60_000,
            location = "Lecture Hall 2",
            latitude = -37.8432,
            longitude = 144.9472,
            category = "Electronics",
            imageUri = ""
        ),
        LostFoundItem(
            type = "Lost",
            name = "Brown Dog",
            phone = "0400000005",
            description = "Small brown dog with red collar.",
            createdAtMillis = System.currentTimeMillis() - 4 * 60_000,
            location = "North Car Park",
            latitude = -37.8388,
            longitude = 144.9480,
            category = "Pets",
            imageUri = sampleImageUri
        )
    )

    val categoryOptions by remember {
        derivedStateOf {
            listOf("All") + items.map { it.category }.distinct().sorted()
        }
    }

    val filteredItems by remember {
        derivedStateOf {
            items.filter { item ->
                val categoryMatch = selectedCategory == "All" || item.category == selectedCategory
                val query = searchQuery.trim()
                val queryMatch = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)
                categoryMatch && queryMatch
            }
        }
    }

    val refreshItems = {
        items.clear()
        items.addAll(databaseHelper.getAllItems())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshItems()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lost & Found", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { controlsExpanded = !controlsExpanded }) {
                Text("☰")
            }
        }
        Button(onClick = onCreatePostClick) {
            Text("Create a post")
        }

        if (controlsExpanded && BuildConfig.DEBUG) {
            Button(onClick = {
                databaseHelper.insertItem(quickLostItem)
                refreshItems()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Quick Add Lost Item")
            }

            Button(onClick = {
                databaseHelper.insertItem(quickFoundItem)
                refreshItems()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Quick Add Found Item")
            }

            Button(onClick = {
                databaseHelper.insertItems(quickBulkItems)
                refreshItems()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Quick Add Multiple Items")
            }

            Button(onClick = {
                databaseHelper.clearAllItems()
                refreshItems()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear Test Data")
            }
        }

        if (controlsExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search items") },
                placeholder = { Text("Name, description, or location") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoryOptions.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

            Button(
                onClick = {
                    searchQuery = ""
                    selectedCategory = "All"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear filters")
            }
        }

        if (filteredItems.isEmpty()) {
            Text(
                text = "No results found. Try a different search or clear filters.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                factory = { ctx ->
                    RecyclerView(ctx).apply {
                        layoutManager = LinearLayoutManager(ctx)
                        adapter = LostFoundAdapter { item -> onItemClick(item.id) }
                    }
                },
                update = { recyclerView ->
                    (recyclerView.adapter as? LostFoundAdapter)?.submitList(filteredItems)
                }
            )
        }
    }
}
