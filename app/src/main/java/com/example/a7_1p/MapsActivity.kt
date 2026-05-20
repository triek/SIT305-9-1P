package com.example.a7_1p

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import com.example.a7_1p.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var databaseHelper: LostFoundDatabaseHelper
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var radiusOptionsKm = listOf(1, 2, 5, 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = LostFoundDatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.radiusSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            radiusOptionsKm.map { "$it km" }
        )

        binding.showNearbyButton.setOnClickListener {
            filterNearbyOnMap()
        }

        binding.clearMapFilterButton.setOnClickListener {
            reloadAllItemMarkers()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        reloadAllItemMarkers()
    }

    private fun reloadAllItemMarkers() {
        val map = googleMap ?: return
        val validItems = databaseHelper.getAllItems().filter { it.hasValidCoordinates() }
        map.clear()

        if (validItems.isEmpty()) {
            Toast.makeText(this, "No saved items with valid coordinates to display.", Toast.LENGTH_SHORT).show()
            return
        }

        validItems.forEach { addItemMarker(map, it) }
        val firstPosition = LatLng(validItems.first().latitude, validItems.first().longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 14f))
    }

    @SuppressLint("MissingPermission")
    private fun filterNearbyOnMap() {
        val map = googleMap ?: return
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Toast.makeText(this, "Location permission is required for map radius filter.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadiusMeters = radiusOptionsKm[binding.radiusSpinner.selectedItemPosition] * 1_000
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { userLocation ->
                if (userLocation == null) {
                    Toast.makeText(this, "Could not get your location.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val nearbyItems = databaseHelper.getAllItems()
                    .filter { it.hasValidCoordinates() }
                    .filter { item ->
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            userLocation.latitude,
                            userLocation.longitude,
                            item.latitude,
                            item.longitude,
                            results
                        )
                        results[0] <= selectedRadiusMeters
                    }

                map.clear()
                val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
                map.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )

                nearbyItems.forEach { addItemMarker(map, it) }
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))

                if (nearbyItems.isEmpty()) {
                    Toast.makeText(this, "No nearby items found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to fetch current location.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addItemMarker(map: GoogleMap, item: LostFoundItem) {
        val position = LatLng(item.latitude, item.longitude)
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(item.name)
                .snippet("${item.type} • ${item.category}")
        )
    }

    private fun LostFoundItem.hasValidCoordinates(): Boolean {
        val inRange = latitude in -90.0..90.0 && longitude in -180.0..180.0
        val notDefaultZero = latitude != 0.0 || longitude != 0.0
        return inRange && notDefaultZero
    }
}
