package com.example.a7_1p

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import com.example.a7_1p.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var databaseHelper: LostFoundDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = LostFoundDatabaseHelper(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val items = databaseHelper.getAllItems()
        val validItems = items.filter { it.hasValidCoordinates() }

        if (validItems.isEmpty()) {
            Toast.makeText(
                this,
                "No saved items with valid coordinates to display.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        validItems.forEach { item ->
            val position = LatLng(item.latitude, item.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(item.name)
                    .snippet("${item.type} • ${item.category}")
            )
        }

        val firstMarker = validItems.first()
        val firstPosition = LatLng(firstMarker.latitude, firstMarker.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 14f))
    }

    private fun LostFoundItem.hasValidCoordinates(): Boolean {
        val inRange = latitude in -90.0..90.0 && longitude in -180.0..180.0
        val notDefaultZero = latitude != 0.0 || longitude != 0.0
        return inRange && notDefaultZero
    }
}
