package com.example.chatmessengerapp.module

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Converted to a data class and added imageUrl
data class Messages (
    val sender : String? = "",
    val receiver : String? = "",
    val message : String? = "",
    @ServerTimestamp val time : Date? = null,
    val imageUrl: String? = ""
){
    // Note: This ID is not unique if multiple messages have the exact same content and time.
    // It's generally better to use the document ID from Firestore if a unique ID is needed.
    val id : String get() = "$sender-$receiver-$message-$time"

}