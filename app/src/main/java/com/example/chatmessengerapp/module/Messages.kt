package com.example.chatmessengerapp.module

data class Messages(
    var sender: String? = "",
    var receiver: String? = "",
    var message: String? = "",
    var time: String? = "",
    var imageUrl: String? = null // New field for image URL
)
