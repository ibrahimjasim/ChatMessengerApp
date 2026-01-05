package com.example.chatmessengerapp

import com.google.firebase.auth.FirebaseAuth

object Utils {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getUidLoggedIn(): String {
        return auth.currentUser?.uid ?: ""
    }
}