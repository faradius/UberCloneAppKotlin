package com.alex.ubercloneapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityCalificationBinding
import com.alex.ubercloneapp.models.History
import com.alex.ubercloneapp.providers.HistoryProvider
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.utils.Constants

class CalificationActivity : AppCompatActivity() {

    private var history: History? = null
    private lateinit var binding: ActivityCalificationBinding
    private var historyProvider = HistoryProvider()
    private var calification = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, value, b ->
            calification = value
        }

        binding.btnCalification.setOnClickListener{
            if (history?.id != null){
                updateCalification(history?.id!!)
            }else{
                Toast.makeText(this, "El id del historial es nulo", Toast.LENGTH_LONG).show()
            }
        }

        getHistory()
    }

    private fun updateCalification(idDocument: String){
        historyProvider.updateCalificationToDriver(idDocument, calification).addOnCompleteListener {
            if (it.isSuccessful){
                goToMap()
            }else{
                Toast.makeText(this@CalificationActivity, "Error al actualizar la calificación", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if(query.documents.size > 0){
                    history =  query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id
                    binding.tvOrigin.text = history?.origin
                    binding.tvDestination.text = history?.destination
                    binding.tvPrice.text = "${String.format("%.1f", history?.price)}"
                    binding.tvTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"
                    Log.d("FIRESTORE", "HISTORIAL: ${history?.toJson()}")
                }else{
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}