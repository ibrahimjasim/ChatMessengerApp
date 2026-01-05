package com.example.chatmessengerapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessengerapp.MyApplication
import com.example.chatmessengerapp.SharedPrefs
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.example.chatmessengerapp.module.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatAppViewModel : ViewModel() {

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()
    private val firestore = FirebaseFirestore.getInstance()

    val usersRepo = UserRepository()
    val messageRepo = MessageRepository()
    val recentChatRepo = ChatListRepository()

    init {
        getCurrentUser()
    }

    fun getUsers(): LiveData<List<Users>> {
        return usersRepo.getUser()
    }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        val context = MyApplication.instance.applicationContext
        firestore.collection("Users").document(Utils.getUiLogged())
            .addSnapshotListener { value, error ->
                if (value != null && value.exists() && value.data != null) {
                    val users = value.toObject(Users::class.java)
                    name.postValue(users?.username!!)
                    imageUrl.postValue(users?.imageUrl!!)

                    val mysharedPrefs = SharedPrefs(context)
                    mysharedPrefs.setValue("username", users.username!!)
                }
            }
    }

    // Send Message
    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val context = MyApplication.instance.applicationContext

            val hasMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to message.value!!,
                "time" to Utils.getTime()
            )

            val uniqueIdList = listOf(sender, receiver).sorted()
            val uniqueId = uniqueIdList.joinToString(separator = "")

            val friendnamesplit = friendname.split("\\s".toRegex())[0]
            val mysharedPrefs = SharedPrefs(context)
            mysharedPrefs.setValue("friendid", receiver)
            mysharedPrefs.setValue("chatroomid", uniqueId)
            mysharedPrefs.setValue("friendname", friendnamesplit)
            mysharedPrefs.setValue("friendimage", friendimage)

            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(Utils.getTime()).set(hasMap).addOnCompleteListener { taskmessage ->
                    if (taskmessage.isSuccessful) {
                        val setHashap = hashMapOf<String, Any>(
                            "friendid" to receiver,
                            "time" to Utils.getTime(),
                            "sender" to Utils.getUiLogged(), // Ändrat från getUidLoggedIn
                            "message" to message.value!!,
                            "friendsimage" to friendimage,
                            "name" to friendname,
                            "person" to "you"
                        )

                        firestore.collection("Conversation${Utils.getUiLogged()}").document(receiver) // Ändrat
                            .set(setHashap)

                        firestore.collection("Conversation${receiver}").document(Utils.getUiLogged()) // Ändrat
                            .update(
                                "message", message.value!!,
                                "time", Utils.getTime(),
                                "person", name.value!!
                            )

                        message.postValue("")
                    }
                }
        }

    fun getMessages(friendid: String): LiveData<List<Messages>> {
        return messageRepo.getMessages(friendid)
    }
}
