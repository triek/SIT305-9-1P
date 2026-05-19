package com.example.a7_1p

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.a7_1p.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val deakinBurwood = LatLng(-37.8473, 145.1149)

        googleMap.addMarker(
            MarkerOptions()
                .position(deakinBurwood)
                .title("Deakin Burwood Test Marker")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deakinBurwood, 15f))
    }
}
