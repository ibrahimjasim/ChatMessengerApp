package com.example.chatmessengerapp.module

class Messages (

    val sender : String? = "",
    val receiver : String? = "",
    val message : String? = "",
    val time : String? = "",
){

    val id : String get() = "$sender-$receiver-$message-$time"

}