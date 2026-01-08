package com.example.chatmessengerapp.notifications.entity

data class PushNotification(
    val data: NotificationData = NotificationData(),
    val to: String = ""
)