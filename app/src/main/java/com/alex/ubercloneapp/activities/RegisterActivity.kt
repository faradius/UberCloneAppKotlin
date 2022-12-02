package com.alex.ubercloneapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alex.ubercloneapp.utils.Config
import com.alex.ubercloneapp.databinding.ActivityRegisterBinding
import com.alex.ubercloneapp.models.Client
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Config.setVersionCompatibilityStatusBar(window)
        binding.btnRegister.setOnClickListener { funRegister() }

        binding.btnGoToLogin.setOnClickListener{ goToLogin() }
    }

    private fun funRegister(){
        val name = binding.etName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (isValidForm(name,lastName,email, phone,password,confirmPassword)){
            authProvider.register(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    val client = Client(
                        id = authProvider.getId(),
                        name = name,
                        lastname = lastName,
                        phone = phone,
                        email = email
                    )

                    clientProvider.create(client).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            goToMap()
                        }else{
                            Toast.makeText(this@RegisterActivity, "Hubo un error en almacenar los datos del usuario ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                            Log.d("Firebase", "Error: ${it.exception.toString()}")
                        }
                    }

                }else{
                    Toast.makeText(this@RegisterActivity, "Registro fallido ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                    Log.d("Firebase", "Error: ${it.exception.toString()}")
                }
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(
        name:String,
        lastName:String,
        email:String,
        phone:String,
        password:String,
        confirmPassword:String
    ):Boolean{
        if (name.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }else if (lastName.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu apellido", Toast.LENGTH_SHORT).show()
            return false
        }else if (phone.isEmpty()) {
            Toast.makeText(this, "Debes ingresar tu telefono", Toast.LENGTH_SHORT).show()
            return false
        }else if (email.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }else if (password.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }else if (confirmPassword.isEmpty()){
            Toast.makeText(this, "Debes ingresar la confirmación de la contaseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword){
            Toast.makeText(this, "las contraseñas deben de coincidir", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6){
            Toast.makeText(this, "la contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun goToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}