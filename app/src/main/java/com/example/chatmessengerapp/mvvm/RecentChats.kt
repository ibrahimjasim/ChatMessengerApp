package com.example.chatmessengerapp.mvvm

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class RecentChats(
    val friendid: String? = "",
    val friendsimage: String? = "",
    val time: Timestamp? = null,
    val name: String? = "",
    val sender: String? = "",
    val message: String? = "",
    val person: String? = "",
    val status: String? = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(friendid)
        parcel.writeString(friendsimage)
        parcel.writeParcelable(time, flags)
        parcel.writeString(name)
        parcel.writeString(sender)
        parcel.writeString(message)
        parcel.writeString(person)
        parcel.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RecentChats> {
        override fun createFromParcel(parcel: Parcel): RecentChats = RecentChats(parcel)
        override fun newArray(size: Int): Array<RecentChats?> = arrayOfNulls(size)
    }
}