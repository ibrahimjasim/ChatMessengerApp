package com.example.chatmessengerapp.module

import com.google.firebase.Timestamp

data class Messages(
    var sender: String? = "",
    var receiver: String? = "",
    var message: String? = "",
    var time: Timestamp? = null,
    var imageUrl: String? = null
)
