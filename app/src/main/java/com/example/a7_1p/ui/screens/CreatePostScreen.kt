package com.example.a7_1p.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.a7_1p.R
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem

private val categories = listOf("Electronics", "Pets", "Wallets", "Keys", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onPostSaved: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = remember { LostFoundDatabaseHelper(context) }

    var type by rememberSaveable { mutableStateOf("Lost") }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf("") }
    var longitude by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    var imageUri by rememberSaveable { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        imageUri = uri?.toString().orEmpty()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
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
        }, modifier = Modifier.fillMaxWidth()) {
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
            val parsedLatitude = latitude.toDoubleOrNull()
            val parsedLongitude = longitude.toDoubleOrNull()
            val hasErrors = name.isBlank() || phone.isBlank() || description.isBlank() || location.isBlank() || parsedLatitude == null || parsedLongitude == null
            if (hasErrors) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                databaseHelper.insertItem(LostFoundItem(type = type, name = name, phone = phone, description = description, createdAtMillis = System.currentTimeMillis(), location = location, latitude = parsedLatitude!!, longitude = parsedLongitude!!, category = category, imageUri = imageUri))
                Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show()
                onPostSaved()
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
    }
}
