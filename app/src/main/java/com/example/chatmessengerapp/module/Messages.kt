package com.example.chatmessengerapp.module

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

class Messages (

    val sender : String? = "",
    val receiver : String? = "",
    val message : String? = "",
    @ServerTimestamp val time : Date? = null
){

    val id : String get() = "$sender-$receiver-$message-$time"

}