package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessengerapp.MyApplication
import com.example.chatmessengerapp.SharedPrefs
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatAppViewModel : ViewModel(){

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()
    private val firestore = FirebaseFirestore.getInstance()

    val usersRepo = UserRepository()

    init {
        getCurrentUser()
    }


    fun getUsers() : LiveData<List<Users>> {
        return usersRepo.getUser()
    }


    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext

        firestore.collection("Users").document(Utils.getUiLogged())
            .addSnapshotListener { value, error ->


                if (value != null && value.exists() && value.data != null) {

                    val users = value.toObject(Users::class.java)

                    name.value = users?.username!!
                    imageUrl.value = users?.imageUrl!!

                    val mysharedPrefs = SharedPrefs(context)
                    mysharedPrefs.setValue("username", users.username)


                }

            }

    }
}
