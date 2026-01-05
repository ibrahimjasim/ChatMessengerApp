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
        val messageList = MutableLiveData<List<Messages>>()


        val uniqueid = listOf(Utils.getUiLogged(), friendid).sorted()
        uniqueid.joinToString(separator = "")

        firestore.collection("Messages").document(uniqueid.toString()).collection("chats")
            .orderBy("time", Query.Direction.ASCENDING).addSnapshotListener { value, error ->

                if (error != null) {

                    return@addSnapshotListener

                }

                val messageList = mutableListOf<Messages>()

                if (!value!!.isEmpty) {


                    value.documents.forEach { document ->

                        val messageModel = document.toObjekt(Messages::class.java)

                        if (messageModel!!.sender.equals(Utils.getLoggerIn()) && messageModel.receiver.equals(
                                friendid
                            ) ||
                            messageModel.sender.equals(friendid) && messageModel.receiver.equals(
                                Utils.getUidLoggedIn()
                            )
                        ) {
                            messageModel.let {
                                Messages

                                messageList.add(it!!)
                            }
                        }

                    }

                    messages.value = messagesList


                }
            }
        return messages

    }