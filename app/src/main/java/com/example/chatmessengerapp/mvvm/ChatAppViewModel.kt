package com.example.chatmessengerapp.mvvm

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatmessengerapp.MyApplication
import com.example.chatmessengerapp.SharedPrefs
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages
import com.example.chatmessengerapp.module.RecentChats
import com.example.chatmessengerapp.module.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatAppViewModel : ViewModel() {

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    private val firestore = FirebaseFirestore.getInstance()

    private val usersRepo = UserRepository()
    private val messageRepo = MessageRepository()
    private val recentChatRepo = ChatListRepository()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        getCurrentUser()
        // Don't call getRecentUsers() here unless UI observes it.
    }

    fun getUsers(): LiveData<List<Users>> = usersRepo.getUsers()

    fun getMessages(friend: String): LiveData<List<Messages>> =
        messageRepo.getMessages(friend)

    fun getRecentUsers(): LiveData<List<RecentChats>> =
        recentChatRepo.getAllChatList() //
    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {

            val context = MyApplication.instance.applicationContext

            val msg = message.value?.trim().orEmpty()
            if (msg.isEmpty() || receiver.isBlank()) return@launch

            val chatRoomId = listOf(sender, receiver).sorted().joinToString("")

            val hashMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to msg,
                "time" to Utils.getTime()
            )

            val friendnamesplit = friendname.split("\\s".toRegex())[0]
            val mysharedPrefs = SharedPrefs(context)
            mysharedPrefs.setValue("friendid", receiver)
            mysharedPrefs.setValue("chatroomid", chatRoomId)
            mysharedPrefs.setValue("friendname", friendnamesplit)
            mysharedPrefs.setValue("friendimage", friendimage)

            firestore.collection("Messages")
                .document(chatRoomId)
                .collection("chats")
                .document(Utils.getTime())
                .set(hashMap)
                .addOnCompleteListener { taskmessage ->

                    val setHashMap = hashMapOf<String, Any>(
                        "friendid" to receiver,
                        "time" to Utils.getTime(),
                        "sender" to Utils.getUidLoggedIn(),
                        "message" to msg,
                        "friendsimage" to friendimage,
                        "name" to friendname,
                        "person" to "you"
                    )

                    firestore.collection("Conversation${Utils.getUidLoggedIn()}")
                        .document(receiver)
                        .set(setHashMap)

                    firestore.collection("Conversation$receiver")
                        .document(Utils.getUidLoggedIn())
                        .set(
                            hashMapOf(
                                "friendid" to Utils.getUidLoggedIn(),
                                "time" to Utils.getTime(),
                                "sender" to receiver,
                                "message" to msg,
                                "friendsimage" to (imageUrl.value.orEmpty()),
                                "name" to (name.value.orEmpty()),
                                "person" to (name.value.orEmpty())
                            )
                        )

                    if (taskmessage.isSuccessful) {
                        message.postValue("")
                    }
                }
        }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val context = MyApplication.instance.applicationContext

        firestore.collection("Users")
            .document(Utils.getUidLoggedIn())
            .addSnapshotListener { value, _ ->
                if (value != null && value.exists()) {
                    val user = value.toObject(Users::class.java)
                    name.value = user?.username.orEmpty()
                    imageUrl.value = user?.imageUrl.orEmpty()

                    SharedPrefs(context).setValue("username", user?.username.orEmpty())
                }
            }
    }

    fun updateProfile() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {

        val context = MyApplication.instance.applicationContext

        val newName = name.value?.trim().orEmpty()
        val newImage = imageUrl.value?.trim().orEmpty()

        if (newName.isEmpty() || newImage.isEmpty()) return@launch

        val hashMapUser = hashMapOf<String, Any>(
            "username" to newName,
            "imageUrl" to newImage
        )

        firestore.collection("Users")
            .document(Utils.getUidLoggedIn())
            .update(hashMapUser)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                }
            }

        val mysharedPrefs = SharedPrefs(context)
        val friendid = mysharedPrefs.getValue("friendid") ?: return@launch

        val hashMapUpdate = hashMapOf<String, Any>(
            "friendsimage" to newImage,
            "name" to newName,
            "person" to newName
        )

        firestore.collection("Conversation$friendid")
            .document(Utils.getUidLoggedIn())
            .update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
            .document(friendid)
            .update("person", "you")
    }
}