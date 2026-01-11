package com.example.chatmessengerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.chatmessengerapp.SharedPrefs
import com.example.chatmessengerapp.Utils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class NotificationsReply : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = results.getCharSequence(KEY_TEXT_REPLY)?.toString()?.trim().orEmpty()
        if (replyText.isBlank()) return

        val chatRoomId = intent.getStringExtra(EXTRA_CHATROOM_ID).orEmpty()

        // This is the person who sent the original message that you are replying to.
        // In this context, they are the "receiver" of your reply.
        val receiverId = intent.getStringExtra(EXTRA_SENDER_ID).orEmpty()
        val receiverName = intent.getStringExtra(EXTRA_SENDER_NAME).orEmpty()
        val receiverImage = intent.getStringExtra(EXTRA_SENDER_IMAGE).orEmpty()

        // Your ID is the sender's ID.
        val myId = Utils.getUidLoggedIn()

        if (chatRoomId.isBlank() || receiverId.isBlank() || myId.isBlank()) return

        // --- START OF THE FINAL FIXES ---

        val db = FirebaseFirestore.getInstance()
        val serverTimestamp = FieldValue.serverTimestamp() // 1. Use the correct server timestamp.

        // 2. Save the message correctly.
        val messageData = hashMapOf(
            "sender" to myId,
            "receiver" to receiverId,
            "message" to replyText,
            "time" to serverTimestamp
        )

        // Use .add() to let Firestore create a unique ID for the message.
        db.collection("Messages")
            .document(chatRoomId)
            .collection("chats")
            .add(messageData)

        // 3. Update recent chats for YOU (the sender).
        val myRecentChatData = hashMapOf(
            "friendid" to receiverId,
            "time" to serverTimestamp,
            "sender" to myId,
            "message" to replyText,
            "friendsimage" to receiverImage,
            "name" to receiverName,
            "person" to "you"
        )

        db.collection("Conversation$myId")
            .document(receiverId)
            .set(myRecentChatData, SetOptions.merge()) // Use .set() with merge for safety.

        // 4. Update recent chats for the OTHER USER (the receiver).
        // To get your own name and image, we can retrieve them from SharedPrefs.
        val myName = SharedPrefs(context).getValue("username") ?: ""
        // Note: your image URL isn't available here unless you save it in SharedPrefs too.
        // For now, we will leave it out to prevent errors. You can add it later.

        val friendRecentChatData = hashMapOf(
            "friendid" to myId,
            "time" to serverTimestamp,
            "sender" to myId,
            "message" to replyText,
            "name" to myName,
            "person" to myName
            // "friendsimage" would be your image URL.
        )

        db.collection("Conversation$receiverId")
            .document(myId)
            .set(friendRecentChatData, SetOptions.merge()) // Use .set() with merge to be consistent.

        // --- END OF THE FINAL FIXES ---
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"

        const val EXTRA_SENDER_ID = "extra_sender_id"
        const val EXTRA_SENDER_NAME = "extra_sender_name"
        const val EXTRA_SENDER_IMAGE = "extra_sender_image"

        const val EXTRA_RECEIVER_ID = "extra_receiver_id"
        const val EXTRA_CHATROOM_ID = "extra_chatroom_id"
    }
}
