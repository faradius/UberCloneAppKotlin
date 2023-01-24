package com.alex.ubercloneapp.providers

import com.alex.ubercloneapp.api.IFCMApi
import com.alex.ubercloneapp.api.RetrofitClient
import com.alex.ubercloneapp.models.FCMBody
import com.alex.ubercloneapp.models.FCMResponse
import retrofit2.Call

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody):Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }
}