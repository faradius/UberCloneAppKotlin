package com.alex.ubercloneapp.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.databinding.ActivityProfileBinding
import com.alex.ubercloneapp.models.Client
import com.alex.ubercloneapp.providers.AuthProvider
import com.alex.ubercloneapp.providers.ClientProvider
import com.alex.ubercloneapp.utils.Config
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Config.setVersionCompatibilityStatusBar(window)

        getClient()
        binding.ivBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.cvProfileImage.setOnClickListener { selectImage() }
    }

    private fun updateInfo(){
        val name = binding.etName.text.toString()
        val lastname = binding.etLastName.text.toString()
        val phone = binding.etPhone.text.toString()

        val client = Client(
            id = authProvider.getId(),
            name = name,
            lastname = lastname,
            phone = phone
        )

        if (imageFile != null){
            clientProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot->
                clientProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    client.image = imageUrl
                    clientProvider.update(client).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }
        }else{
            clientProvider.update(client).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                binding.tvEmail.text = client?.email
                binding.etName.setText(client?.name)
                binding.etLastName.setText(client?.lastname)
                binding.etPhone.setText(client?.phone)

                if(client?.image != null){
                    if (client?.image != ""){
                        Glide.with(this).load(client?.image).into(binding.cvProfileImage)
                    }
                }
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.cvProfileImage.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }
    }
    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
}