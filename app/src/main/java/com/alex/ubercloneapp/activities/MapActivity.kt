package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}