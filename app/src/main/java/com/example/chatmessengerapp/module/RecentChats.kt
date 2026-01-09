package com.example.chatmessengerapp.module

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecentChats(
    val friendid: String? = "",
    val friendsimage: String? = "",
    val time: Timestamp? = null,
    val name: String? = "",
    val sender: String? = "",
    val message: String? = "",
    val person: String? = "",
    val status: String? = ""
) : Parcelable
