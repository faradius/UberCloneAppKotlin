package com.alex.ubercloneapp.providers

import android.util.Log
import com.alex.ubercloneapp.models.Booking
import com.alex.ubercloneapp.models.History
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {
        return db.add(history).addOnFailureListener{
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getLastHistory(): Query{ //CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idClient", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun updateStatus(idClient:String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener{ exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }
}