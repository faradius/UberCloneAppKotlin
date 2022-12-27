package com.alex.ubercloneapp.activities

import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityTripInfoBinding
import com.alex.ubercloneapp.models.Prices
import com.alex.ubercloneapp.providers.ConfigProvider
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

class TripInfoActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    val TAG = "TripInfoActivity"

    private lateinit var binding: ActivityTripInfoBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null

    private var configProvider = ConfigProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        //EXTRAS
        extraOriginName = intent.getStringExtra(Constants.ORIGIN)!!
        extraDestinationName = intent.getStringExtra(Constants.DESTINATION)!!
        extraOriginLat = intent.getDoubleExtra(Constants.ORIGIN_LAT, 0.0)
        extraOriginLng = intent.getDoubleExtra(Constants.ORIGIN_LNG, 0.0)
        extraDestinationLat = intent.getDoubleExtra(Constants.DESTINATION_LAT, 0.0)
        extraDestinationLng = intent.getDoubleExtra(Constants.DESTINATION_LNG, 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)



        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f

        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false,this)

        binding.tvOrigin.text = extraOriginName
        binding.tvDestination.text = extraDestinationName

        Log.d(TAG, "Localizacion: Origin lat: ${originLatLng?.latitude}")
        Log.d(TAG, "Localizacion: Origin lng: ${originLatLng?.longitude}")
        Log.d(TAG, "Localizacion: Destination lat: ${destinationLatLng?.latitude}")
        Log.d(TAG, "Localizacion: Destination lng: ${destinationLatLng?.longitude}")

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun getPrices(distance: Double, time: Double){
        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()){
                val prices = document.toObject(Prices::class.java) //Obtenemos la información del documento

                val totalDistance = distance * prices?.km!!  //Valor por kilometro
                Log.d("PRICES", "totalDistance: $totalDistance")

                val totalTime = time * prices.min!!  //Valor por minuto
                Log.d("PRICES", "TotalTime: $totalTime")

                var total = totalDistance + totalTime
                Log.d("PRICES", "total: $total")

                total = if(total < 10.0) prices.minValue!! else total
                Log.d("PRICES", "new total: $total")

                var minTotal = total - prices.difference!!
                Log.d("PRICES", "minTotal: $minTotal")
                var maxTotal = total + prices.difference!!
                Log.d("PRICES", "maxTotal: $maxTotal")

                val minTotalString = String.format("%.1f", minTotal)
                val maxTotalString = String.format("%.1f", maxTotal)

                binding.tvPrice.text = "$$minTotalString - $$maxTotalString"

            }
        }
    }

    private fun addOriginMarker(){
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(originLatLng!!).title("Mi posición")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker(){
        markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Llegada")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }

    private fun easyDrawRoute(){
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(originLatLng!!).zoom(14f).build()
        ))
        easyDrawRoute()
        addOriginMarker()
        addDestinationMarker()

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

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        var distance = polyLineDetailsArray[1].distance.toDouble() //Lo devuelve en metros
        var time = polyLineDetailsArray[1].time.toDouble() //Lo devuelve en segundos

        distance = if(distance < 1000.0) 1000.0 else distance // Si es menos de 1000 metros lo dejamos en 1 kilometro
        time = if(time < 60.0) 60.0 else time

        distance = distance / 1000 //KM
        time = time / 60 //Min

        val timeString = String.format("%.2f", time)
        val distanceString = String.format("%.2f", distance)

        getPrices(distance, time)

        binding.tvTimeAndDistance.text = "$timeString mins - $distanceString km"

        directionUtil.drawPath(WAY_POINT_TAG)
    }
}