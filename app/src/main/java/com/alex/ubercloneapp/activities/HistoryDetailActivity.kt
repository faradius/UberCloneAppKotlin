package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityHistoryDetailBinding
import com.alex.ubercloneapp.models.Client
import com.alex.ubercloneapp.models.Driver
import com.alex.ubercloneapp.models.History
import com.alex.ubercloneapp.providers.ClientProvider
import com.alex.ubercloneapp.providers.DriverProvider
import com.alex.ubercloneapp.providers.HistoryProvider
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.utils.RelativeTime
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.toObject

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var driverProvider = DriverProvider()
    private var extraId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        extraId = intent.getStringExtra("id")!!
        getHistory()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->
            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.tvOrigin.text = history?.origin
                binding.tvDestination.text = history?.destination
                binding.tvDate.text = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoryDetailActivity)
                binding.tvPrice.text = "$${String.format("%.1f", history?.price)}"
                binding.tvMyCalification.text = "${history?.calificationToDriver}"
                binding.tvClientCalification.text = "${history?.calificationToClient}"
                binding.tvTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                getDriverInfo(history?.idDriver!!)
            }
        }
    }

    private fun getDriverInfo(id: String){
        driverProvider.getDriver(id).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.tvEmail.text = driver?.email
                binding.tvName.text = "${driver?.name} ${driver?.lastname}"

                if (driver?.image != null){
                    if (driver?.image != ""){
                        Glide.with(this).load(driver?.image).into(binding.cvProfileImage)
                    }
                }
            }
        }
    }
}