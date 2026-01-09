package com.example.chatmessengerapp.mvvm

import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatAppViewModel : ViewModel() {

    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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

    fun getMessages(friend: String): LiveData<List<Messages>> = messageRepo.getMessages(friend)

    fun getRecentUsers(): LiveData<List<RecentChats>> = recentChatRepo.getAllChatList()

    fun sendTextMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val msg = message.value?.trim()
            if (msg.isNullOrEmpty()) return@launch

            val chatRoomId = listOf(sender, receiver).sorted().joinToString("")
            val time = Utils.getTime()

            val messageData = hashMapOf(
                "sender" to sender,
                "receiver" to receiver,
                "message" to msg,
                "time" to time,
                "imageUrl" to null
            )

            val myProfile = firestore.collection("Users").document(sender).get().await().toObject(Users::class.java)

            val recentSenderHashMap = hashMapOf(
                "friendid" to receiver, "time" to time, "sender" to sender, "message" to msg,
                "friendsimage" to friendimage, "name" to friendname, "person" to "you"
            )

            val recentReceiverHashMap = hashMapOf(
                "friendid" to sender, "time" to time, "sender" to sender, "message" to msg,
                "friendsimage" to (myProfile?.imageUrl ?: ""), "name" to (myProfile?.username ?: ""),
                "person" to (myProfile?.username ?: "")
            )

            try {
                firestore.runBatch { batch ->
                    val newMessageRef = firestore.collection("Messages").document(chatRoomId).collection("chats").document()
                    batch.set(newMessageRef, messageData)
                    val senderRecentRef = firestore.collection("Conversation\${sender}").document(receiver)
                    batch.set(senderRecentRef, recentSenderHashMap)
                    val receiverRecentRef = firestore.collection("Conversation\${receiver}").document(sender)
                    batch.set(receiverRecentRef, recentReceiverHashMap)
                }.await()
                message.postValue("")
            } catch (e: Exception) {
                Log.e("ChatAppViewModel", "Error sending text message", e)
            }
        }

    fun sendImageMessage(sender: String, receiver: String, friendname: String, friendimage: String, imageUri: Uri) =
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val storageRef = storage.reference.child("chat_images/${UUID.randomUUID()}")
                val uploadTask = storageRef.putFile(imageUri).await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                val chatRoomId = listOf(sender, receiver).sorted().joinToString("")
                val time = Utils.getTime()
                val imageMessageText = "Image"

                val messageData = hashMapOf(
                    "sender" to sender, "receiver" to receiver, "message" to null,
                    "time" to time, "imageUrl" to imageUrl
                )

                val myProfile = firestore.collection("Users").document(sender).get().await().toObject(Users::class.java)

                val recentSenderHashMap = hashMapOf(
                    "friendid" to receiver, "time" to time, "sender" to sender, "message" to imageMessageText,
                    "friendsimage" to friendimage, "name" to friendname, "person" to "you"
                )

                val recentReceiverHashMap = hashMapOf(
                    "friendid" to sender, "time" to time, "sender" to sender, "message" to imageMessageText,
                    "friendsimage" to (myProfile?.imageUrl ?: ""), "name" to (myProfile?.username ?: ""),
                    "person" to (myProfile?.username ?: "")
                )

                firestore.runBatch { batch ->
                    val newMessageRef = firestore.collection("Messages").document(chatRoomId).collection("chats").document()
                    batch.set(newMessageRef, messageData)
                    val senderRecentRef = firestore.collection("Conversation\${sender}").document(receiver)
                    batch.set(senderRecentRef, recentSenderHashMap)
                    val receiverRecentRef = firestore.collection("Conversation\${receiver}").document(sender)
                    batch.set(receiverRecentRef, recentReceiverHashMap)
                }.await()
            } catch (e: Exception) {
                Log.e("ChatAppViewModel", "Error sending image message", e)
            }
        }

    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val context = MyApplication.instance.applicationContext
        firestore.collection("Users").document(Utils.getUidLoggedIn()).addSnapshotListener { value, _ ->
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
        val updates = hashMapOf<String, Any>()
        val newName = name.value?.trim().orEmpty()
        val newImage = imageUrl.value?.trim().orEmpty()

        if (newName.isNotEmpty()) {
            updates["username"] = newName
        }
        if (newImage.isNotEmpty()) {
            updates["imageUrl"] = newImage
        }

        if (updates.isEmpty()) {
            Toast.makeText(context, "Nothing to update", Toast.LENGTH_SHORT).show()
            return@launch
        }

        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(updates).addOnSuccessListener {
            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
        }
    }
}
