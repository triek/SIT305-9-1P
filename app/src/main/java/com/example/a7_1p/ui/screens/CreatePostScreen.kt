package com.example.a7_1p.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import com.google.android.gms.location.LocationServices

private val categories = listOf("Electronics", "Pets", "Wallets", "Keys", "Other")
private val buttonBackground = Color(0xFFBDBDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onPostSaved: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = remember { LostFoundDatabaseHelper(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var type by rememberSaveable { mutableStateOf("Lost") }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var selectedLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var imageUri by rememberSaveable { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { currentLocation ->
                if (currentLocation != null) {
                    selectedLatitude = currentLocation.latitude
                    selectedLongitude = currentLocation.longitude
                    latitude = currentLocation.latitude.toString()
                    longitude = currentLocation.longitude.toString()
                    location = "Current Location"
                } else {
                    Toast.makeText(context, "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        imageUri = uri?.toString().orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create Lost/Found Post", style = MaterialTheme.typography.headlineSmall)
        Text("Type", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = type == "Lost", onClick = { type = "Lost" })
            Text("Lost", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = type == "Found", onClick = { type = "Found" })
            Text("Found")
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name*") }, isError = showErrors && name.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone number*") }, isError = showErrors && phone.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description*") }, isError = showErrors && description.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location name/address*") }, isError = showErrors && location.isBlank(), modifier = Modifier.fillMaxWidth())
        OutlinedButton(
            onClick = {
                if (hasLocationPermission()) {
                    fetchCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = buttonBackground, contentColor = Color.Black),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("GET CURRENT LOCATION")
        }
        OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Latitude*") }, isError = showErrors && latitude.toDoubleOrNull() == null, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Longitude*") }, isError = showErrors && longitude.toDoubleOrNull() == null, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
            OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                categories.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false })
                }
            }
        }

        OutlinedButton(onClick = {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = buttonBackground, contentColor = Color.Black),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(if (imageUri.isBlank()) "Pick an image" else "Change image")
        }

        if (imageUri.isNotBlank()) {
            AndroidView(
                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                update = { imageView -> imageView.setImageURI(Uri.parse(imageUri)) },
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Button(onClick = {
            showErrors = true
            val parsedLatitude = selectedLatitude ?: latitude.toDoubleOrNull()
            val parsedLongitude = selectedLongitude ?: longitude.toDoubleOrNull()
            val hasErrors = name.isBlank() || phone.isBlank() || description.isBlank() || location.isBlank() || parsedLatitude == null || parsedLongitude == null
            if (hasErrors) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                databaseHelper.insertItem(LostFoundItem(type = type, name = name, phone = phone, description = description, createdAtMillis = System.currentTimeMillis(), location = location, latitude = parsedLatitude, longitude = parsedLongitude, category = category, imageUri = imageUri))
                Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show()
                onPostSaved()
            }
        },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = buttonBackground, contentColor = Color.Black),
            border = BorderStroke(1.dp, Color.Black)
        ) { Text("Save") }
    }
}
