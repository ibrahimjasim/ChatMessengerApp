package com.example.chatmessengerapp.notifications.entity

data class NotificationData(
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val receiverId: String = "",
    val chatRoomId: String = "",
    val message: String = ""
)