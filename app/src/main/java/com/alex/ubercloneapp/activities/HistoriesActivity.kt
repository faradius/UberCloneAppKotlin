package com.alex.ubercloneapp.activities


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alex.ubercloneapp.adapters.HistoriesAdapter
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityHistoriesBinding
import com.alex.ubercloneapp.models.History
import com.alex.ubercloneapp.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityHistoriesBinding
    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter: HistoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvHistories.layoutManager = linearLayoutManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Historial de viajes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        getHistories()
    }

    private fun getHistories(){
        //es bueno limpiar la lista antes de mostrar los elementos
        histories.clear()
        historyProvider.getHistories().get().addOnSuccessListener { query->
            if (query != null){
                if (query.documents.size > 0){
                    val documents = query.documents

                    for (d in documents){
                        var history = d.toObject(History::class.java)
                        //Se le agrega el id pero a la lista de historiales convertido a objeto
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this@HistoriesActivity, histories)
                    binding.rvHistories.adapter = adapter
                }
            }

        }
    }
}