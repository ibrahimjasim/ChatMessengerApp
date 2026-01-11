package com.example.chatmessengerapp.mvvm

import android.util.Log
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

                if (error != null) {
                    Log.e("MessageRepository", "Listen failed.", error)
                    messagesLiveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    messagesLiveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                val messageList = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Automatic and safe deserialization. This handles Timestamp to Date conversion
                        // and protects against missing fields.
                        doc.toObject(Messages::class.java)
                    } catch (e: Exception) {
                        Log.e("MessageRepository", "Error converting message document ${doc.id}", e)
                        null // Filter out any documents that fail to parse
                    }
                }
                messagesLiveData.postValue(messageList)
            }

        return messagesLiveData
    }
}