package com.alex.ubercloneapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivitySearchBinding
import com.alex.ubercloneapp.utils.Config

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Config.setVersionCompatibilityStatusBar(window)
    }
}