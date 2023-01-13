package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.ubercloneapp.R

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.alex.ubercloneapp.databinding.ActivityMapTripBinding
import com.alex.ubercloneapp.models.Booking
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.BookingProvider
import com.alex.ubercloneapp.providers.GeoProvider
import com.alex.ubercloneapp.utils.CarMoveAnim
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.utils.Constants
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener,
    DirectionUtil.DirectionCallBack {

    private var listenerDriverLocation: ListenerRegistration? = null
    private var driverLocation: LatLng? = null
    private var endLatLng: LatLng? = null
    private var startLatLng: LatLng? = null

    private var listenerBooking: ListenerRegistration? = null
    private var markerDestination: Marker? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? = null
    private val TAG = "LOCALIZACIÓN"

    private lateinit var binding: ActivityMapTripBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()


    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil


    private var isDriverLocationFound = false
    private var isBookingLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapTripBinding.inflate(layoutInflater)
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

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )


    }

    val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d(TAG, "Permiso concedido")


                    }
                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d(TAG, "Permiso concedido con limitación")


                    }
                    else -> {
                        Log.d(TAG, "Permiso no concedido")
                    }
                }
            }
        }

    private fun getLocationDriver() {
        if (booking != null) {
            listenerDriverLocation = geoProvider.getLocationWorking(booking?.idDriver!!)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        Log.d("FIRESTORE", "Error: ${error.message}")
                        //Deje de escuchar cuando haya encontrado un error
                        return@addSnapshotListener
                    }

                    if (driverLocation != null) {
                        endLatLng = driverLocation
                    }

                    if (document?.exists()!!) {
                        //nos trae la información de la latitud y logitud en forma de arreglo y para acceder
                        //a los datos es necesario convertirlo en una lista
                        var l = document?.get("l") as List<*>
                        val lat = l[0] as Double
                        val lng = l[1] as Double

                        driverLocation = LatLng(lat, lng)

                        //Cuando al se esta ejecutando en tiempo real y solo se quiere que se ejecute una sola vez
                        //podemos hacer esta validación dentro de un addSnapshotListener
                        if (!isDriverLocationFound && driverLocation != null) {
                            isDriverLocationFound = true
                            addDriverMarker(driverLocation!!)
                            easyDrawRoute(driverLocation!!, originLatLng!!)
                        }

                        if (endLatLng != null) {
                            CarMoveAnim.carAnim(markerDriver!!, endLatLng!!, driverLocation!!)
                        }

                        Log.d("FIRESTORE", "LOCATION: $l")
                    }
                }
        }

    }

    private fun getBooking() {
        //el metodo get nos trae la información una sola vez, mientras el addSnapshotListener nos va a traer la información en tiempo real
        listenerBooking = bookingProvider.getBooking().addSnapshotListener { document, error ->
            if (error != null) {
                Log.d("FIRESTORE", "Error: ${error.message}")
                //Deje de escuchar cuando haya encontrado un error
                return@addSnapshotListener
            }

            booking = document?.toObject(Booking::class.java)

            //Para que se ejecute una sola vez
            if (!isBookingLoaded) {
                isBookingLoaded = true
                originLatLng = LatLng(booking?.originLat!!, booking?.originLng!!)
                destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(originLatLng!!).zoom(17f).build()
                    )
                )
                getLocationDriver()
                addOriginMarker(originLatLng!!)
            }

            if (booking?.status == "accept") {
                binding.tvStatus.text = "Aceptado"
            } else if (booking?.status == "started") {
                startTrip()
            } else if (booking?.status == "finished") {
                finishTrip()
            }
        }
    }

    private fun finishTrip() {
        listenerDriverLocation?.remove()
        binding.tvStatus.text = "Finalizado"
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun startTrip() {
        binding.tvStatus.text = "Iniciado"
        googleMap?.clear()
        if (driverLocation != null) {
            addDriverMarker(driverLocation!!)
            addDestinationMarker()
            easyDrawRoute(driverLocation!!, destinationLatLng!!)
        }
    }

    private fun easyDrawRoute(originLatLng: LatLng, destinationLatLng: LatLng) {
        wayPoints.clear()
        wayPoints.add(originLatLng)
        wayPoints.add(destinationLatLng)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng)
            .build()

        directionUtil.initPath()
    }

    private fun addOriginMarker(position: LatLng) {
        markerOrigin = googleMap?.addMarker(
            MarkerOptions().position(position).title("Recoger aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person))
        )
    }

    private fun addDriverMarker(position: LatLng) {
        markerDriver = googleMap?.addMarker(
            MarkerOptions().position(position).title("Tu conductor")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car))
        )
    }

    private fun addDestinationMarker() {
        if (destinationLatLng != null) {
            markerDestination = googleMap?.addMarker(
                MarkerOptions().position(destinationLatLng!!).title("Recoger aqui")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin))
            )
        }
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            70,
            150,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, 70, 150)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerBooking?.remove()
        listenerDriverLocation?.remove()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        getBooking()
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

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )

            if (!success!!) {
                Log.d(TAG, "onMapReady: No se pudo encontrar el estilo")
            }

        } catch (e: Resources.NotFoundException) {
            Log.d(TAG, "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    //Actualización de la posición en tiempo real
    override fun currentLocation(location: Location) {

    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}