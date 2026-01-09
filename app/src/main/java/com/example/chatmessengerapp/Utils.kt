package com.example.chatmessengerapp

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    private val auth = FirebaseAuth.getInstance()

    fun getUidLoggedIn(): String {
        return auth.uid ?: ""
    }

    fun getTime(): Timestamp {
        return Timestamp.now()
    }

    fun formatTime(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    const val REQUEST_IMAGE_CAPTURE = 1
    const val REQUEST_IMAGE_PICK = 2
    const val CHANNEL_ID = "chat_messages"
}
