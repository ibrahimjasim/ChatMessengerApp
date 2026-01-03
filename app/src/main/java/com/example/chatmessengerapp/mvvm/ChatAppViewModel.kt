package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.MutableLiveData

class ChatAppViewModel {

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()


}