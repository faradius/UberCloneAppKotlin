package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivitySearchBinding
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.GeoProvider
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()

    //Busqueda del conductor
    private var radius = 0.1
    private var idDriver = ""
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

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        getClosesDriver()
    }

    //Este metodo busca a los alrededores del usuario en un radio determinado
    private fun getClosesDriver(){
        geoProvider.getNearbyDrivers(originLatLng!!, radius).addGeoQueryEventListener(object: GeoQueryEventListener{

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if (!isDriverFound){
                    isDriverFound = true
                    idDriver = documentID
                    Log.d("FIRESTORE", "Conductor id: $idDriver")
                    driverLatLng = LatLng(location.latitude, location.longitude)
                    binding.tvSearch.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"

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
}