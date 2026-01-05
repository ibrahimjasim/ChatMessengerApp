package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Users
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private var firestore = FirebaseFirestore.getInstance()


    fun getUser(): LiveData<List<Users>> {

        val users = MutableLiveData<List<Users>>()


        firestore.collection("Users").addSnapshotListener { snapshot, exception ->


            if (exception != null) {
                return@addSnapshotListener
            }

            val usersList = mutableListOf<Users>()
            snapshot?.documents?.forEach { document ->

                val user = document.toObject(Users::class.java)

                // Check if the user is not the currently logged-in user
                if (user != null && user.userid != Utils.getUiLogged()) {
                    usersList.add(user)
                }
            }
            // Update LiveData outside the loop
            users.value = usersList
        }

        return users
    }
}
