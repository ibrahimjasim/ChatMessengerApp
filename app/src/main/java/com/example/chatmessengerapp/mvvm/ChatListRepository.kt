package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatListRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllChatList(): LiveData<List<RecentChats>> {

        val mainChatList = MutableLiveData<List<RecentChats>>()

        val uid = Utils.getUidLoggedIn()

        firestore.collection("Conversation$uid")
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    // You can log it if you want
                    // Log.e("ChatListRepository", "Firestore error", exception)
                    return@addSnapshotListener
                }

                val chatList = snapshot?.documents
                    ?.mapNotNull { it.toObject(RecentChats::class.java) }
                    ?.filter { it.sender == uid }
                    ?: emptyList()

                mainChatList.value = chatList
            }

        return mainChatList
    }
}