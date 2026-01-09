package com.example.chatmessengerapp.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.RecentChats
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllChatList(): LiveData<List<RecentChats>> {

        val mainChatList = MutableLiveData<List<RecentChats>>()

        firestore
            .collection("Conversation${Utils.getUidLoggedIn()}")
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("ChatListRepository", "Listen failed.", error)
                    mainChatList.postValue(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    mainChatList.postValue(emptyList())
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<RecentChats>()

                for (document in snapshot.documents) {
                    try {
                        // Manual deserialization to avoid crashing on old data
                        val friendid = document.getString("friendid")
                        val friendsimage = document.getString("friendsimage")
                        val time = document.getTimestamp("time") // This will be null if it's a String
                        val name = document.getString("name")
                        val sender = document.getString("sender")
                        val message = document.getString("message")
                        val person = document.getString("person")
                        val status = document.getString("status")

                        // If time is null, it means we found old data. Skip it.
                        if (time == null) {
                            Log.w("ChatListRepository", "Skipping document with invalid time format: ${document.id}")
                            continue
                        }

                        val recentModel = RecentChats(
                            friendid, friendsimage, time, name, sender, message, person, status
                        )
                        chatList.add(recentModel)

                    } catch (e: Exception) {
                        // This catch block will handle any other unexpected errors for a single document
                        Log.e("ChatListRepository", "Error converting document ${document.id}", e)
                    }
                }

                mainChatList.postValue(chatList)
            }

        return mainChatList
    }
}