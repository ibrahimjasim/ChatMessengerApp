package com.example.chatmessengerapp.module

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class RecentChats(val friendid : String? ="",
                       val friendsimage: String? = "",
                       @ServerTimestamp val time : Date? = null,
                       val name: String? ="",
                       val sender: String? = "",
                       val message : String? = "",
                       val person: String? = "",
                       val status: String? ="",

                       ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong().let { if (it == -1L) null else Date(it) },
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(friendid)
        parcel.writeString(friendsimage)
        parcel.writeLong(time?.time ?: -1L)
        parcel.writeString(name)
        parcel.writeString(sender)
        parcel.writeString(message)
        parcel.writeString(person)
        parcel.writeString(status)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecentChats> {
        override fun createFromParcel(parcel: Parcel): RecentChats {
            return RecentChats(parcel)
        }

        override fun newArray(size: Int): Array<RecentChats?> {
            return arrayOfNulls(size)
        }
    }


}