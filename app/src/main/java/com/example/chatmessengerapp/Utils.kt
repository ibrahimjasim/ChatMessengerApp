package com.example.chatmessengerapp

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date

object Utils {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

        private val auth = FirebaseAuth.getInstance()

        fun getUiLogged(): String {
            // If auth.currentUser is not null, return its uid. Otherwise, return an empty string.
            return auth.currentUser?.uid ?: ""
        }


        fun getTime(): String {


            val formatter = SimpleDateFormat("HH:mm:ss")
            val date: Date = Date(System.currentTimeMillis())
            val stringdate = formatter.format(date)


            return stringdate



    }




}
