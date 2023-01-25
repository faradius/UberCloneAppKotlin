package com.alex.ubercloneapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivitySearchBinding
import com.alex.ubercloneapp.models.Booking
import com.alex.ubercloneapp.models.Driver
import com.alex.ubercloneapp.models.FCMBody
import com.alex.ubercloneapp.models.FCMResponse
import com.alex.ubercloneapp.providers.*
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private var listenerBooking: ListenerRegistration? = null
    private lateinit var binding: ActivitySearchBinding

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0
    private var extraDistance = 0.0
    private var extraTime = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val notificationProvider = NotificationProvider()
    private val driverProvider = DriverProvider()

    //Busqueda del conductor
    private var radius = 0.1
    private var idDriver = ""
    private var driver:Driver? = null
    private var isDriverFound = false
    private var driverLatLng:LatLng? = null
    private var limitRadius = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Config.setVersionCompatibilityStatusBar(window)

        //EXTRAS
        extraOriginName = intent.getStringExtra(Constants.ORIGIN)!!
        extraDestinationName = intent.getStringExtra(Constants.DESTINATION)!!
        extraOriginLat = intent.getDoubleExtra(Constants.ORIGIN_LAT, 0.0)
        extraOriginLng = intent.getDoubleExtra(Constants.ORIGIN_LNG, 0.0)
        extraDestinationLat = intent.getDoubleExtra(Constants.DESTINATION_LAT, 0.0)
        extraDestinationLng = intent.getDoubleExtra(Constants.DESTINATION_LNG, 0.0)
        extraTime = intent.getDoubleExtra(Constants.TIME, 0.0)
        extraDistance = intent.getDoubleExtra(Constants.DISTANCE, 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        getClosesDriver()
        checkIfDriverAccept()
    }

    private fun sendNotification(){

        val map = HashMap<String, String>()
        map.put("title", "Solicitud de viaje")
        map.put("body", "Un cliente esta solicitando un viaje a ${String.format("%.1f", extraDistance)}km y ${String.format("%.1f", extraTime)}Min")

        map.put("idBooking", authProvider.getId())

        val body = FCMBody(
            to = driver?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )
        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                if (response.body() != null){
                    if(response.body()!!.success == 1){
                        Toast.makeText(this@SearchActivity, "Se envio la notificación", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this@SearchActivity, "No se pudo enviar la notificación", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(this@SearchActivity, "Hubo un error enviando la notificación", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("Notification", "Error: ${t.message}")
            }

        })
    }

    private fun checkIfDriverAccept(){
        listenerBooking = bookingProvider.getBooking().addSnapshotListener { snapshot, error ->

            if (error != null){
                Log.d("FIRESTORE", "Error: ${error.message}")
                //Deje de escuchar cuando haya encontrado un error
                return@addSnapshotListener
            }

            //El snapshot exist es permitido por que se esta haciendo referencia a un documento,
            //pero en el conductor no se puede por que se esta haciendo una consulta a una lista de documentos por lo que no es permitido
            if(snapshot != null && snapshot.exists()){
                val booking = snapshot.toObject(Booking::class.java)

                if (booking?.status == "accept"){
                    Toast.makeText(this@SearchActivity, "Viaje Aceptado", Toast.LENGTH_SHORT).show()
                    listenerBooking?.remove()
                    goToMapTrip()

                }else if (booking?.status == "cancel"){
                    Toast.makeText(this@SearchActivity, "Viaje cancelado", Toast.LENGTH_SHORT).show()
                    listenerBooking?.remove()
                    goToMap()
                }
            }
        }
    }

    private fun goToMapTrip(){
        val i = Intent(this, MapTripActivity::class.java)
        startActivity(i)
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun createBooking(idDriver: String){
        val booking = Booking(
            idClient = authProvider.getId(),
            idDriver = idDriver,
            status = "create",
            destination = extraDestinationName,
            origin = extraOriginName,
            time = extraTime,
            km = extraDistance,
            originLat = extraOriginLat,
            originLng = extraOriginLng,
            destinationLat = extraDestinationLat,
            destinationLng = extraDestinationLng
        )

        bookingProvider.create(booking).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this@SearchActivity, "Datos del viaje creados", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@SearchActivity, "Error al crear los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDriverInfo(){
        driverProvider.getDriver(idDriver).addOnSuccessListener { document ->
            if(document.exists()){
                driver = document.toObject(Driver::class.java)
                sendNotification()
            }
        }
    }

    //Este metodo busca a los alrededores del usuario en un radio determinado
    private fun getClosesDriver(){
        geoProvider.getNearbyDrivers(originLatLng!!, radius).addGeoQueryEventListener(object: GeoQueryEventListener{

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if (!isDriverFound){
                    isDriverFound = true
                    idDriver = documentID
                    getDriverInfo()
                    Log.d("FIRESTORE", "Conductor id: $idDriver")
                    driverLatLng = LatLng(location.latitude, location.longitude)
                    binding.tvSearch.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"
                    createBooking(documentID)

                }
            }

            override fun onGeoQueryError(exception: Exception) {

            }

            //Este metodo se ejecuta cuando termina la busqueda,
            override fun onGeoQueryReady() {
                //Si no encuentra un conductor en la busqueda
                if(!isDriverFound){
                    radius += 0.1

                    if (radius > limitRadius){
                        binding.tvSearch.text = "NO SE ENCONTRO NINGUN CONDUCTOR"
                        return
                    }else{
                        //Esto es para volver ejecutar el onKeyEntered pero con el radio incrementado
                        getClosesDriver()
                    }
                }

            }

            override fun onKeyExited(documentID: String) {

            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {

            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerBooking?.remove()
    }
}