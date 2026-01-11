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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    }

    fun getUsers(): LiveData<List<Users>> = usersRepo.getUsers()

    fun getMessages(friend: String): LiveData<List<Messages>> =
        messageRepo.getMessages(friend)

    fun getRecentUsers(): LiveData<List<RecentChats>> =
        recentChatRepo.getAllChatList()

    fun sendMessage(senderId: String, receiverId: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val context = MyApplication.instance.applicationContext
            val msg = message.value?.trim().orEmpty()
            if (msg.isBlank() || senderId.isBlank() || receiverId.isBlank()) {
                Log.e("ChatAppViewModel", "Cannot send message with blank sender, receiver, or message.")
                return@launch
            }

            val serverTimestamp = FieldValue.serverTimestamp()

            val messageData = hashMapOf<String, Any>(
                "sender" to senderId,
                "receiver" to receiverId,
                "message" to msg,
                "time" to serverTimestamp
            )

            val myRecentChatData = hashMapOf<String, Any>(
                "friendid" to receiverId,
                "time" to serverTimestamp,
                "sender" to senderId,
                "message" to msg,
                "friendsimage" to friendimage,
                "name" to friendname,
                "person" to "you"
            )

            val friendRecentChatData = hashMapOf<String, Any>(
                "friendid" to senderId,
                "time" to serverTimestamp,
                "sender" to senderId,
                "message" to msg,
                "friendsimage" to (imageUrl.value.orEmpty()),
                "name" to (name.value.orEmpty()),
                "person" to (name.value.orEmpty())
            )

            val chatRoomId = listOf(senderId, receiverId).sorted().joinToString("")

            firestore.collection("Messages")
                .document(chatRoomId)
                .collection("chats")
                .add(messageData)
                .addOnCompleteListener { taskmessage ->
                    if (taskmessage.isSuccessful) {
                        firestore.collection("Conversation$senderId")
                            .document(receiverId)
                            .set(myRecentChatData, SetOptions.merge())

                        firestore.collection("Conversation$receiverId")
                            .document(senderId)
                            .set(friendRecentChatData, SetOptions.merge())

                        message.postValue("")
                    } else {
                        Log.e("ChatAppViewModel", "Failed to send message", taskmessage.exception)
                    }
                }
        }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) {
            Log.e("ChatAppViewModel", "Current user ID is blank in getCurrentUser.")
            return@launch
        }

        firestore.collection("Users")
            .document(uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("ChatAppViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (value != null && value.exists()) {
                    val user = value.toObject(Users::class.java)
                    name.postValue(user?.username.orEmpty())
                    imageUrl.postValue(user?.imageUrl.orEmpty())
                    SharedPrefs(MyApplication.instance.applicationContext).setValue("username", user?.username.orEmpty())
                }
            }
    }

    // --- THIS IS THE FINAL, CORRECTED FUNCTION ---
    fun updateProfile() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val context = MyApplication.instance.applicationContext
        val newName = name.value?.trim().orEmpty()
        val newImage = imageUrl.value?.trim().orEmpty()

        if (newName.isEmpty() && newImage.isEmpty()) {
            return@launch
        }

        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) {
            Log.e("ChatAppViewModel", "Cannot update profile, user ID is blank.")
            return@launch
        }

        val userUpdates = hashMapOf<String, Any>(
            "username" to newName,
            "imageUrl" to newImage
        )

        // The ONLY job of this function is to update the Users collection.
        firestore.collection("Users")
            .document(uid)
            .update(userUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Update failed.", Toast.LENGTH_SHORT).show()
                    Log.e("ChatAppViewModel", "Profile update failed.", task.exception)
                }
            }
        // I have REMOVED the old, buggy code that was writing to the "Conversation" collections.
    }
}
