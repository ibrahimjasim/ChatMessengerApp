package com.example.chatmessengerapp

import com.google.firebase.auth.FirebaseAuth

class Utils {

    companion object {

        private val auth = FirebaseAuth.getInstance()

        fun getUiLogged(): String {
            // If auth.currentUser is not null, return its uid. Otherwise, return an empty string.
            return auth.currentUser?.uid ?: ""
        }
    }
}
