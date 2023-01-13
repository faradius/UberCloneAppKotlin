package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityCalificationBinding
import com.alex.ubercloneapp.models.History
import com.alex.ubercloneapp.providers.HistoryProvider
import com.alex.ubercloneapp.utils.Constants

class CalificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationBinding
    private var historyProvider = HistoryProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getHistory()
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if(query.documents.size > 0){
                    val history =  query.documents[0].toObject(History::class.java)
                    Log.d("FIRESTORE", "HISTORIAL: ${history?.toJson()}")
                }else{
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}