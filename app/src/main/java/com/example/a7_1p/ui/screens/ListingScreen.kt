package com.example.a7_1p.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a7_1p.R
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingScreen(
    onCreatePostClick: () -> Unit,
    onShowOnMapClick: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val databaseHelper = remember { LostFoundDatabaseHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val items = remember { mutableStateListOf<LostFoundItem>() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var controlsExpanded by remember { mutableStateOf(false) }
    var radiusExpanded by remember { mutableStateOf(false) }
    var selectedRadiusKm by remember { mutableStateOf(1) }
    var activeRadiusMeters by remember { mutableStateOf<Int?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var nextSingleLostIndex by remember { mutableStateOf(0) }
    var nextSingleFoundIndex by remember { mutableStateOf(0) }

    val radiusOptionsKm = listOf(1, 2, 5, 10)

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

                val radiusMatch = if (activeRadiusMeters != null && userLocation != null && item.hasValidCoordinates()) {
                    val distanceResults = FloatArray(1)
                    Location.distanceBetween(
                        userLocation!!.latitude,
                        userLocation!!.longitude,
                        item.latitude,
                        item.longitude,
                        distanceResults
                    )
                    distanceResults[0] <= activeRadiusMeters!!
                } else {
                    activeRadiusMeters == null
                }

                categoryMatch && queryMatch && radiusMatch
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
        Button(
            onClick = onCreatePostClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("CREATE A NEW ADVERT") }

        Button(onClick = onShowOnMapClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("SHOW ON MAP") }


        if (controlsExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search items") },
                placeholder = { Text("Name, description, or location") },
                modifier = Modifier.fillMaxWidth()
            )


            var testDataExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = testDataExpanded,
                onExpandedChange = { testDataExpanded = !testDataExpanded }
            ) {
                OutlinedTextField(
                    value = "Test data actions",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Test items") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testDataExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = testDataExpanded,
                    onDismissRequest = { testDataExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add 1 lost item") },
                        onClick = {
                            val item = singleLostTestItems[nextSingleLostIndex]
                            databaseHelper.insertItem(item)
                            nextSingleLostIndex = (nextSingleLostIndex + 1) % singleLostTestItems.size
                            refreshItems()
                            testDataExpanded = false
                            Toast.makeText(context, "Added 1 lost item", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add 1 found item") },
                        onClick = {
                            val item = singleFoundTestItems[nextSingleFoundIndex]
                            databaseHelper.insertItem(item)
                            nextSingleFoundIndex = (nextSingleFoundIndex + 1) % singleFoundTestItems.size
                            refreshItems()
                            testDataExpanded = false
                            Toast.makeText(context, "Added 1 found item", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add 3 lost items") },
                        onClick = {
                            databaseHelper.insertItems(multiLostTestItems)
                            refreshItems()
                            testDataExpanded = false
                            Toast.makeText(context, "Added 3 lost items", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add 3 found items") },
                        onClick = {
                            databaseHelper.insertItems(multiFoundTestItems)
                            refreshItems()
                            testDataExpanded = false
                            Toast.makeText(context, "Added 3 found items", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear all items") },
                        onClick = {
                            val deletedCount = databaseHelper.clearAllItems()
                            refreshItems()
                            testDataExpanded = false
                            Toast.makeText(context, "Cleared $deletedCount items", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            ExposedDropdownMenuBox(
                expanded = radiusExpanded,
                onExpandedChange = { radiusExpanded = !radiusExpanded }
            ) {
                OutlinedTextField(
                    value = "$selectedRadiusKm km",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Radius") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = radiusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = radiusExpanded,
                    onDismissRequest = { radiusExpanded = false }
                ) {
                    radiusOptionsKm.forEach { radiusKm ->
                        DropdownMenuItem(
                            text = { Text("$radiusKm km") },
                            onClick = {
                                selectedRadiusKm = radiusKm
                                radiusExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    getCurrentLocation(
                        onLocationFound = { location ->
                            userLocation = location
                            activeRadiusMeters = selectedRadiusKm * 1_000
                            if (filteredItems.isEmpty()) {
                                Toast.makeText(context, "No nearby items found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        fusedLocationClient = fusedLocationClient,
                        context = context
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("SEARCH NEARBY") }

            Button(
                onClick = {
                    activeRadiusMeters = null
                    userLocation = null
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("CLEAR RADIUS FILTER") }

            Button(
                onClick = {
                    searchQuery = ""
                    selectedCategory = "All"
                    activeRadiusMeters = null
                    userLocation = null
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear filters") }
        }

        if (filteredItems.isEmpty()) {
            Text(
                text = if (activeRadiusMeters != null) {
                    "No nearby items found in the selected radius."
                } else {
                    "No results found. Try a different search or clear filters."
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(top = 4.dp),
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

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    onLocationFound: (Location) -> Unit,
    onError: (String) -> Unit,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: android.content.Context
) {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    if (!hasFine && !hasCoarse) {
        onError("Location permission is required for nearby search.")
        return
    }

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationFound(location)
            } else {
                onError("Could not get your location.")
            }
        }
        .addOnFailureListener {
            onError("Unable to fetch current location.")
        }
}

private fun LostFoundItem.hasValidCoordinates(): Boolean {
    val inRange = latitude in -90.0..90.0 && longitude in -180.0..180.0
    val notDefaultZero = latitude != 0.0 || longitude != 0.0
    return inRange && notDefaultZero
}


private val singleLostTestItems = listOf(
    buildTestItem(type = "Lost", name = "Alex's Black Wallet (close)", category = "Documents", distanceBand = "close"),
    buildTestItem(type = "Lost", name = "Noah's Blue Backpack (mid)", category = "Accessories", distanceBand = "mid"),
    buildTestItem(type = "Lost", name = "Liam's White AirPods Case (far)", category = "Electronics", distanceBand = "far")
)

private val singleFoundTestItems = listOf(
    buildTestItem(type = "Found", name = "Emma's Silver Keys (close)", category = "Keys", distanceBand = "close"),
    buildTestItem(type = "Found", name = "Sophia's Student ID Card (mid)", category = "Documents", distanceBand = "mid"),
    buildTestItem(type = "Found", name = "Olivia's Red Umbrella (far)", category = "Accessories", distanceBand = "far")
)

private val multiLostTestItems = listOf(
    buildTestItem(type = "Lost", name = "Mason's Green Water Bottle (close)", category = "Accessories", distanceBand = "close"),
    buildTestItem(type = "Lost", name = "Ethan's Grey Laptop Sleeve (mid)", category = "Electronics", distanceBand = "mid"),
    buildTestItem(type = "Lost", name = "Lucas's Brown Notebook (far)", category = "Documents", distanceBand = "far")
)

private val multiFoundTestItems = listOf(
    buildTestItem(type = "Found", name = "Ava's Pink Scarf (close)", category = "Accessories", distanceBand = "close"),
    buildTestItem(type = "Found", name = "Mia's Black Glasses Case (mid)", category = "Accessories", distanceBand = "mid"),
    buildTestItem(type = "Found", name = "Charlotte's USB Drive (far)", category = "Electronics", distanceBand = "far")
)

private fun buildTestItem(type: String, name: String, category: String, distanceBand: String): LostFoundItem {
    val baseLat = -37.8136
    val baseLon = 144.9631
    val offset = when (distanceBand) {
        "close" -> 0.001
        "mid" -> 0.012
        else -> 0.045
    }

    return LostFoundItem(
        type = type,
        name = name,
        phone = "0400123456",
        description = "Test item for UI feature verification.",
        createdAtMillis = System.currentTimeMillis(),
        location = "Melbourne CBD",
        latitude = baseLat + offset,
        longitude = baseLon + offset,
        category = category,
        imageUri = ""
    )
}
