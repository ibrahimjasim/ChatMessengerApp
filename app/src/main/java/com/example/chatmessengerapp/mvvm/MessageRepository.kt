package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessageRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getMessages(friendid: String): LiveData<List<Messages>> {
        val messagesLiveData = MutableLiveData<List<Messages>>()


        val uniqueIdList = listOf(Utils.getUiLogged(), friendid).sorted()
        val chatRoomId = uniqueIdList.joinToString(separator = "")

        firestore.collection("Messages").document(chatRoomId).collection("chats")
            .orderBy("time", Query.Direction.ASCENDING).addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val tempList = mutableListOf<Messages>()

                if (value != null && !value.isEmpty) {
                    value.documents.forEach { document ->
                        val messageModel = document.toObject(Messages::class.java)

                        if (messageModel != null) {

                            if ((messageModel.sender == Utils.getUiLogged() && messageModel.receiver == friendid) ||
                                (messageModel.sender == friendid && messageModel.receiver == Utils.getUiLogged())) {
                                tempList.add(messageModel)
                            }
                        }
                    }
                    messagesLiveData.value = tempList
                }
            }
        return messagesLiveData
    }
}
