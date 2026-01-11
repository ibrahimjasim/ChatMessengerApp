package com.example.chatmessengerapp.mvvm

import android.graphics.Bitmap
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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

    fun getMessages(friend: String): LiveData<List<Messages>> =
        messageRepo.getMessages(friend)

    fun getRecentUsers(): LiveData<List<RecentChats>> =
        recentChatRepo.getAllChatList()

    fun sendImage(senderId: String, receiverId: String, friendname: String, friendimage: String, bitmap: Bitmap) = 
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val context = MyApplication.instance.applicationContext
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val data = baos.toByteArray()

            val imageRef = storage.reference.child("chat_images/${UUID.randomUUID()}.jpg")

            imageRef.putBytes(data)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        sendMessage(senderId, receiverId, friendname, friendimage, imageUrl = imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Image upload failed.", Toast.LENGTH_SHORT).show()
                    Log.e("ChatAppViewModel", "Image upload failed", e)
                }
    }

    fun sendMessage(
        senderId: String, 
        receiverId: String, 
        friendname: String, 
        friendimage: String, 
        messageText: String? = null, 
        imageUrl: String? = null
    ) = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
        val context = MyApplication.instance.applicationContext
        val msg = messageText ?: message.value?.trim().orEmpty()

        if (msg.isBlank() && imageUrl.isNullOrBlank()) {
            Log.e("ChatAppViewModel", "Cannot send an empty message.")
            return@launch
        }

        if (senderId.isBlank() || receiverId.isBlank()) {
            Log.e("ChatAppViewModel", "Cannot send message with blank sender or receiver.")
            return@launch
        }

        val serverTimestamp = FieldValue.serverTimestamp()

        val messageData = hashMapOf<String, Any>(
            "sender" to senderId,
            "receiver" to receiverId,
            "time" to serverTimestamp
        )
        if (msg.isNotBlank()) messageData["message"] = msg
        if (!imageUrl.isNullOrBlank()) messageData["imageUrl"] = imageUrl

        val recentMessageText = if (msg.isNotBlank()) msg else "Image"

        val myRecentChatData = hashMapOf<String, Any>(
            "friendid" to receiverId,
            "time" to serverTimestamp,
            "sender" to senderId,
            "message" to recentMessageText,
            "friendsimage" to friendimage,
            "name" to friendname,
            "person" to "you"
        )

        val friendRecentChatData = hashMapOf<String, Any>(
            "friendid" to senderId,
            "time" to serverTimestamp,
            "sender" to senderId,
            "message" to recentMessageText,
            "friendsimage" to (this@ChatAppViewModel.imageUrl.value.orEmpty()),
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

                    if (messageText == null) {
                        message.postValue("")
                    }
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

        val userUpdates = hashMapOf<String, Any>()
        if (newName.isNotEmpty()) userUpdates["username"] = newName
        if (newImage.isNotEmpty()) userUpdates["imageUrl"] = newImage


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
    }
}