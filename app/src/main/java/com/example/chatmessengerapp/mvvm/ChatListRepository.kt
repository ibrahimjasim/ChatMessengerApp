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

                val chatList = snapshot.documents.mapNotNull { document ->
                    try {
                        // Automatic deserialization. Firestore's toObject will use the default constructor
                        // and map fields. This is safer against schema changes.
                        document.toObject(RecentChats::class.java)
                    } catch (e: Exception) {
                        // This catch block will handle any other unexpected errors for a single document
                        Log.e("ChatListRepository", "Error converting document ${document.id}", e)
                        null // Return null for items that fail to parse, mapNotNull will filter them out.
                    }
                }

                mainChatList.postValue(chatList)
            }

        return mainChatList
    }
}