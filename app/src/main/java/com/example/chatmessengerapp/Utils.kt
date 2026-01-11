package com.example.chatmessengerapp

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Utils {

    companion object {

        @SuppressLint("StaticFieldLeak")
        val context = MyApplication.instance.applicationContext

        @SuppressLint("StaticFieldLeak")
        val firestore = FirebaseFirestore.getInstance()

        private val auth = FirebaseAuth.getInstance()

        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2

        const val MESSAGE_RIGHT = 1
        const val MESSAGE_LEFT = 2

        // Notification channel ID (used in Phase 1 notifications)
        const val CHANNEL_ID = "com.example.chatmessenger"

        fun getUidLoggedIn(): String {
            return FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        }

        fun getTime(): String {
            val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            return formatter.format(Date(System.currentTimeMillis()))
        }
    }
}