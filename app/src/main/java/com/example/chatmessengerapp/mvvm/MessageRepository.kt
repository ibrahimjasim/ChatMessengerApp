package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessageRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getMessages(friendId: String): LiveData<List<Messages>> {
        val messagesLiveData = MutableLiveData<List<Messages>>()

        val chatRoomId = listOf(Utils.getUidLoggedIn(), friendId)
            .sorted()
            .joinToString(separator = "")

        firestore.collection("Messages")
            .document(chatRoomId)
            .collection("chats")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    messagesLiveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Messages::class.java)
                }

                messagesLiveData.postValue(list)
            }

        return messagesLiveData
    }
}