package com.alex.ubercloneapp.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.activities.*
import com.alex.ubercloneapp.models.Booking
import com.alex.ubercloneapp.models.Client
import com.alex.ubercloneapp.models.Driver
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.ClientProvider
import com.alex.ubercloneapp.providers.DriverProvider
import com.alex.ubercloneapp.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    private var driver: Driver? = null
    private lateinit var booking: Booking
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    var tvClientName: TextView? = null
    var tvOrigin: TextView? = null
    var tvDestination: TextView? = null
    var ivPhone: ImageView? = null
    var cvProfileClient: CircleImageView? = null

    val REQUEST_PHONE_CALL = 30


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)
        tvClientName = view.findViewById(R.id.tvDrivertName)
        tvOrigin = view.findViewById(R.id.tvOrigin)
        tvDestination = view.findViewById(R.id.tvDestination)
        ivPhone = view.findViewById(R.id.ivPhone)
        cvProfileClient = view.findViewById(R.id.cvProfileDriver)

        //getDriver()
        val data = arguments?.getString(Constants.BOOKING)
        booking = Booking.fromJson(data!!)!!

        tvOrigin?.text = booking.origin
        tvDestination?.text = booking.destination
        ivPhone?.setOnClickListener {
            if (driver?.phone != null){

                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
                }
                call(driver?.phone!!)
            }
        }

        getDriverInfo()
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PHONE_CALL){
            if(driver?.phone != null){
                call(driver?.phone!!)
            }
        }
    }
    private fun call(phone:String){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:$phone")

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }

        requireActivity().startActivity(i)
    }

    private fun getDriverInfo(){
        driverProvider.getDriver(booking?.idDriver!!).addOnSuccessListener { document ->
            if (document.exists()){
                driver = document.toObject(Driver::class.java)
                tvClientName?.text = "${driver?.name} ${driver?.lastname}"

                if (driver?.image != null){
                    if (driver?.image != ""){
                        Glide.with(requireActivity()).load(driver?.image).into(cvProfileClient!!)
                    }
                }
//                tvUserName?.text = "${driver?.name} ${driver?.lastname}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }
}