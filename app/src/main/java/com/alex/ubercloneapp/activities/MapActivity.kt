package com.alex.ubercloneapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
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
import com.alex.ubercloneapp.models.DriverLocation
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.GeoProvider
import com.alex.ubercloneapp.utils.CarMoveAnim
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
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

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

    private val driversMarkers = ArrayList<Marker>()
    private val driversLocation = ArrayList<DriverLocation>()


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

    private fun getNearbyDrivers(){
        if (myLocationLatLng == null) return

        //El radio son 10 kilometros - Este metodo es util para vendtly
        geoProvider.getNearbyDrivers(myLocationLatLng!!, 5.0).addGeoQueryEventListener(object: GeoQueryEventListener{

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                Log.d(TAG, "onKeyEntered: FireStore Document ID - $documentID")
                Log.d(TAG, "onKeyEntered: Location - $location")

                for (marker in driversMarkers){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            return
                        }
                    }
                }

                //Creamos un nuevo marcador para el conductor conectado
                val driverLatLng = LatLng(location.latitude, location.longitude)
                val marker = googleMap?.addMarker(
                    MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.uber_car)
                    )
                )

                marker?.tag =  documentID

                driversMarkers.add(marker!!)

                val dl = DriverLocation()
                dl.id = documentID
                driversLocation.add(dl)
            }

            override fun onKeyExited(documentID: String) {
                for (marker in driversMarkers){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            marker.remove()
                            driversMarkers.remove(marker)
                            driversLocation.removeAt(getPositionDriver(documentID))
                            return
                        }
                    }
                }
            }

            //Se ejecuta este metodo cuando la ubicación del conductor cambie (metodo en tiempo real)
            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                for (marker in driversMarkers){
                    //Capturar posición inicial y final para animar la posición del conductor
                    val start = LatLng(location.latitude, location.longitude)
                    var end: LatLng? = null
                    val position = getPositionDriver(marker.tag.toString())

                    //Si ya tiene definido una etiqueta
                    if (marker.tag != null){
                        if (marker.tag == documentID){
//                            marker.position = LatLng(location.latitude, location.longitude)
                            if (driversLocation[position].latlng != null){
                                end = driversLocation[position].latlng
                            }
                            driversLocation[position].latlng = LatLng(location.latitude, location.longitude)
                            if (end != null){
                                CarMoveAnim.carAnim(marker, end, start)
                            }
                        }
                    }
                }
            }

            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {

            }

        })
    }

    private fun getPositionDriver(id:String): Int{
        var position = 0
        for (i in driversLocation.indices){
            if (id == driversLocation[i].id){
                position = i
                break
            }
        }
        return position
    }

    private fun onCameraMove(){
        googleMap?.setOnCameraIdleListener { 
            try {
                val geocoder = Geocoder(this)
                originLatLng = googleMap?.cameraPosition?.target

                if(originLatLng != null){
                    val addressList = geocoder.getFromLocation(originLatLng?.latitude!!, originLatLng?.longitude!!,1)

                    if (addressList.size > 0){
                        val city = addressList[0].locality
                        val country = addressList[0].countryName
                        val address = addressList[0].getAddressLine(0)
                        originName = "$address $city"
                        autocompleteOrigin?.setText("$address $city")
                    }
                }
            }catch (e: java.lang.Exception){
                Log.d(TAG, "Error: ${e.message}")
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
        onCameraMove()
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
        googleMap?.isMyLocationEnabled = false

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

        //Se ejecute una sola vez
        if (!isLocationEnabled){
            isLocationEnabled = true
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationLatLng!!).zoom(15f).build()
            ))
            getNearbyDrivers()
            limitSearch()
        }
    }

    override fun locationCancelled() {

    }


}