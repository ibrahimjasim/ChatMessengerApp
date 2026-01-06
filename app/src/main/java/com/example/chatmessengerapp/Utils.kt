package com.example.chatmessengerapp

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getUiLogged(): String {

        return auth.currentUser?.uid ?: ""
    }

    fun getTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return formatter.format(date)
    }
}
