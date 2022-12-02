package com.alex.ubercloneapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.databinding.ActivityMainBinding
import com.alex.ubercloneapp.providers.AuthProvider


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Config.setVersionCompatibilityStatusBar(window)


        binding.btnGoToRegister.setOnClickListener {
            goToRegister()
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        
        if (isValidForm(email,password)){
            authProvider.login(email, password).addOnCompleteListener{
                if (it.isSuccessful){
                    goToMap()
                }
                else{
                    Toast.makeText(this@MainActivity, "Error iniciando sesión", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "login: ERROR: ${it.exception.toString()}")
                }
            }
            //Toast.makeText(this, "Formulario valido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(email: String, password:String):Boolean{

        if (email.isEmpty()){
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }else if (password.isEmpty()){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()

        if (authProvider.existSession()){
            goToMap()
        }
    }
}