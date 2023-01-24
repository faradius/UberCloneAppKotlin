package com.alex.ubercloneapp.services

import android.util.Log
import com.alex.ubercloneapp.channel.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaginClient:FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"]
        val body = data["body"]

        Log.d("NOTIFICATION", "DATA: $data")
        Log.d("NOTIFICATION", "TITLE: $title")
        Log.d("NOTIFICATION", "BODY: $body")

        if (!title.isNullOrBlank() && !body.isNullOrBlank()){
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String){
        val helper = NotificationHelper(baseContext)
        val builder = helper.getNotification(title, body)
        //el id que se define hace que cada notificación que llega se sobre escribirá
        //para que cada notificación sea diferente se tiene que autogenerar un nuevo id
        helper.getManager().notify(1, builder.build())
    }
}