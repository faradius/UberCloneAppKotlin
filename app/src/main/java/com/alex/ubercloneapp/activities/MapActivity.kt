package com.alex.ubercloneapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityMapBinding
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.GeoProvider
import com.alex.ubercloneapp.utils.Config
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.SphericalUtil

class MapActivity : AppCompatActivity(), OnMapReadyCallback,Listener {
    private val TAG = "LOCALIZACIÓN"

    private lateinit var binding: ActivityMapBinding
    private var googleMap:GoogleMap? = null
    private var easyWayLocation:EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()

    //GOOGLE PLACES
    private var places:PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null
    private var originName = ""
    private var destinationName = ""
    private var originLatLng:LatLng? = null
    private var destinationLatLng:LatLng? = null

    private var isLocationEnabled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
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

        locationPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        starGooglePlaces()
    }

    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido")
                    easyWayLocation?.startLocation()

                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "Permiso concedido con limitación")
                    easyWayLocation?.startLocation()

                }
                else -> {
                    Log.d(TAG, "Permiso no concedido")
                }
            }
        }
    }

    private fun starGooglePlaces(){
        if (!Places.isInitialized()){
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        }

        places = Places.createClient(this)
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()
    }

    private fun limitSearch(){
        val northSide = SphericalUtil.computeOffset(myLocationLatLng, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myLocationLatLng, 5000.0, 180.0)

        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        autocompleteDestination?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
    }

    private fun instanceAutocompleteOrigin(){
        autocompleteOrigin = supportFragmentManager.findFragmentById(R.id.placesAutoCompleteOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
                Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
            )
        )
        autocompleteOrigin?.setHint("Lugar de recogida")
        autocompleteOrigin?.setCountry("MX")
        autocompleteOrigin?.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLatLng = place.latLng
                Log.d(TAG, "onPlaceSelected: Address - $originName")
                Log.d(TAG, "onPlaceSelected: Latitud - ${originLatLng?.latitude}")
                Log.d(TAG, "onPlaceSelected: Longitud - ${originLatLng?.longitude}")
            }

            override fun onError(p0: Status) {
                
            }
        })
    }

    private fun instanceAutocompleteDestination(){
        autocompleteDestination = supportFragmentManager.findFragmentById(R.id.placesAutoCompleteDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )
        autocompleteDestination?.setHint("Destino")
        autocompleteDestination?.setCountry("MX")
        autocompleteDestination?.setOnPlaceSelectedListener(object: PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLatLng = place.latLng
                Log.d(TAG, "onPlaceSelected: Address - $destinationName")
                Log.d(TAG, "onPlaceSelected: Latitud - ${destinationLatLng?.latitude}")
                Log.d(TAG, "onPlaceSelected: Longitud - ${destinationLatLng?.longitude}")
            }

            override fun onError(p0: Status) {

            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
//        easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //Desactivar el marcador por defecto de google
        googleMap?.isMyLocationEnabled = true

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

    //Actualización de la posición en tiempo real
    override fun currentLocation(location: Location) {
        //Obteniendo la latitud y longitud de la posición actual
        myLocationLatLng = LatLng(location.latitude, location.longitude)

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
        ))

        //Se ejecute una sola vez
        if (!isLocationEnabled){
            isLocationEnabled = true
            limitSearch()
        }
    }

    override fun locationCancelled() {

    }


}