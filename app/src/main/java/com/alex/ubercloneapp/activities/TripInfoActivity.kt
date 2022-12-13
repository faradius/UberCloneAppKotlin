package com.alex.ubercloneapp.activities

import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityTripInfoBinding
import com.alex.ubercloneapp.utils.Config
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions

class TripInfoActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    val TAG = "TripInfoActivity"

    private lateinit var binding: ActivityTripInfoBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f

        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false,this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        try{
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,R.raw.style)
            )

            if (!success!!){
                Log.d(TAG, "onMapReady: No se pudo encontrar el estilo")
            }

        }catch (e: Resources.NotFoundException){
            Log.d(TAG, "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }
}