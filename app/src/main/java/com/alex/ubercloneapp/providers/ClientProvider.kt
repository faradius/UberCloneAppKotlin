package com.alex.ubercloneapp.providers

import android.net.Uri
import android.util.Log
import com.alex.ubercloneapp.models.Client
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class ClientProvider {

    val db = Firebase.firestore.collection("Clients")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(client: Client): Task<Void>{
        return  db.document(client.id!!).set(client)
    }

    fun getClientById(id: String): Task<DocumentSnapshot>{
        return db.document(id).get()
    }

    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child(id).child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun createToken(idClient: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful){
                //Trae el token de notificaciones y es necesario para el envio de notificaciones
                //de dispositivo a dispositivo
                val token = it.result
                updateToken(idClient, token)
            }
        }
    }

    fun updateToken(idClient:String, token:String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()

        map["token"] = token
        return db.document(idClient).update(map)
    }

    fun update(client:Client): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()

        // Establecer valores en el mapa
        map["name"] = client?.name!!
        map["lastname"] = client?.lastname!!
        map["phone"] = client?.phone!!

        // Si el conductor tiene una imagen, agregarla al mapa
        if (client?.image != null) {
            map["image"] = client?.image!!
        }

        // Actualizar el documento en la base de datos
        return db.document(client?.id!!).update(map)
    }
}