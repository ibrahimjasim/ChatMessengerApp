package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatListRepository {

    val firestore = FirebaseFirestore.getInstance()


    fun getAllChatList(): LiveData<List<RecentChats>> {

        val mainChatList = MutableLiveData<List<RecentChats>>()


        // SHOWING THE RECENT MESSAGED PERSON ON TOP
        firestore.collection("Conversation${Utils.getUidLoggedIn()}").orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->


                if (error != null) {

                    return@addSnapshotListener
                }


                val chatlist = mutableListOf<RecentChats>()

                value?.forEach { document ->

                    val recentmodal = document.toObject(RecentChats::class.java)


                    if (recentmodal.sender.equals(Utils.getUidLoggedIn())) {


                        recentmodal.let {


                            chatlist.add(it)








                        }


                    }







                }


                mainChatList.value = chatlist


            }

        return mainChatList


    }
}