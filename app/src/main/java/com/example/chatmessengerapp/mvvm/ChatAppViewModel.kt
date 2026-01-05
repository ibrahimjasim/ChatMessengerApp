package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatmessengerapp.module.Users

class ChatAppViewModel {

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    val usersRepo = UserRepository()


    fun getUser() : LiveData<List<Users>> {

        return usersRepo.getUser()


    }




}