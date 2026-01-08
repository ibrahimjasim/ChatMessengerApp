package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.RecentChats
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

                if (error != null || snapshot == null) {
                    mainChatList.postValue(emptyList())
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<RecentChats>()

                for (document in snapshot.documents) {
                    val recentModel = document.toObject(RecentChats::class.java) ?: continue
                    chatList.add(recentModel)
                }

                mainChatList.postValue(chatList)
            }

        return mainChatList
    }
}