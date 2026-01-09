package com.example.chatmessengerapp.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.google.firebase.Timestamp
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

                val messageList = mutableListOf<Messages>()
                for (doc in snapshot.documents) {
                    try {
                        // Manual and safe deserialization to protect against old data format
                        val sender = doc.getString("sender")
                        val receiver = doc.getString("receiver")
                        val messageText = doc.getString("message")
                        val imageUrl = doc.getString("imageUrl")
                        val time = doc.getTimestamp("time") // Safely get Timestamp

                        // If time is null, it's old data with a String format. Skip it.
                        if (time == null) {
                            Log.w("MessageRepository", "Skipping message with invalid time format: ${doc.id}")
                            continue
                        }

                        messageList.add(Messages(sender, receiver, messageText, time, imageUrl))

                    } catch (e: Exception) {
                        Log.e("MessageRepository", "Error converting message document ${doc.id}", e)
                    }
                }
                messagesLiveData.postValue(messageList)
            }

        return messagesLiveData
    }
}