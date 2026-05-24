package com.example.a7_1p.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.a7_1p.R
import com.example.a7_1p.data.DateTimeFormatterUtil
import com.example.a7_1p.data.LostFoundDatabaseHelper

private const val TAG = "ItemDetailScreen"

@Composable
fun ItemDetailScreen(itemId: Long, onItemRemoved: () -> Unit) {
    val context = LocalContext.current
    val db = remember { LostFoundDatabaseHelper(context) }
    var item by remember(itemId) { mutableStateOf(db.getItemById(itemId)) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Item details", style = MaterialTheme.typography.headlineSmall)

        if (item == null) {
            Text("Item not found")
            Button(onClick = onItemRemoved) { Text("Back to list") }
            return@Column
        }

        val currentItem = item!!

        if (currentItem.imageUri.isNotBlank()) {
            AndroidView(
                factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                update = { imageView ->
                    val loaded = try {
                        imageView.setImageURI(Uri.parse(currentItem.imageUri))
                        imageView.drawable != null
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Unable to open image URI: ${currentItem.imageUri}", e)
                        false
                    }
                    if (!loaded) imageView.setImageResource(R.drawable.ic_launcher_foreground)
                },
                modifier = Modifier.fillMaxWidth().height(240.dp)
            )
        }

        Text("Name: ${currentItem.name}")
        Text("Type: ${currentItem.type}")
        Text("Description: ${currentItem.description}")
        Text("Phone: ${currentItem.phone}")
        Text("Posted on: ${DateTimeFormatterUtil.formatForDetail(currentItem.createdAtMillis)}")
        Text("Location: ${currentItem.location}")
        Text("Latitude: ${currentItem.latitude}")
        Text("Longitude: ${currentItem.longitude}")
        Text("Category: ${currentItem.category}")

        Button(onClick = {
            val deleted = db.deleteItem(currentItem.id)
            if (deleted > 0) {
                Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
                onItemRemoved()
            } else {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Remove / Delete")
        }
    }
}
