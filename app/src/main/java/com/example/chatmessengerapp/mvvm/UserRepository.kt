package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Users
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getUsers(): LiveData<List<Users>> {
        val usersLiveData = MutableLiveData<List<Users>>()

        firestore.collection("Users").addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                usersLiveData.postValue(emptyList())
                return@addSnapshotListener
            }

            val usersList = mutableListOf<Users>()
            snapshot.documents.forEach { document ->
                val user = document.toObject(Users::class.java)
                if (user != null && user.userid != Utils.getUidLoggedIn()) {
                    usersList.add(user)
                }
            }

            usersLiveData.postValue(usersList)
        }

        return usersLiveData
    }
}