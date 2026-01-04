package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.example.chatmessengerapp.module.RecentChats
import com.example.chatmessengerapp.module.Users
import com.example.chatmessengerapp.notifications.entity.Token
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private  var firestore = FirebaseFirestore.getInstance()


    fun getUser(): LiveData<List<Users>> {

        val users = MutableLiveData<List<Users>>()


        firestore.collection("users").addSnapshotListener { snapshot, exception->


            if (exception != null) {

                return@addSnapshotListener
            }


            val usersList =  mutableListOf<Users>()
            snapshot?.documents?.forEach { document ->

                val user = document.toObject(Users::class.java)

            if (users!!.userid !=Utils.getUiLoggedIn()){
                user.let {
                    userList.add(it)
                }

            }

                users.value = userList
            }

        }

        return users
    }


}