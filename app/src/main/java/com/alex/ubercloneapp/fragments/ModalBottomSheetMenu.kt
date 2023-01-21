package com.alex.ubercloneapp.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.alex.ubercloneapp.R
import com.alex.ubercloneapp.activities.*
import com.alex.ubercloneapp.models.Booking
import com.alex.ubercloneapp.models.Client
import com.alex.ubercloneapp.providers.*
import com.alex.ubercloneapp.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    var tvUserName: TextView? = null
    var lyLogout: LinearLayout? = null
    var lyProfile: LinearLayout? = null
    var lyHistory: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)
        tvUserName = view.findViewById(R.id.tvUserName)
        lyLogout = view.findViewById(R.id.lyLogout)
        lyProfile = view.findViewById(R.id.lyProfile)
        lyHistory = view.findViewById(R.id.lyHistory)

        getClient()
        lyLogout?.setOnClickListener { goToMain() }
        lyProfile?.setOnClickListener { goToProfile() }
        lyHistory?.setOnClickListener { goToHistories() }
        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToHistories(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                tvUserName?.text = "${client?.name} ${client?.lastname}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetMenu"
    }
}