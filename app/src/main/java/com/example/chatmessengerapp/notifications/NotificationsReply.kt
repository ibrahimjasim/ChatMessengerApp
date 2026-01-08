package com.example.chatmessengerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.chatmessengerapp.Utils
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsReply : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = results.getCharSequence(KEY_TEXT_REPLY)?.toString()?.trim().orEmpty()
        if (replyText.isBlank()) return

        val chatRoomId = intent.getStringExtra(EXTRA_CHATROOM_ID).orEmpty()

        // The person who sent the notification message
        val senderId = intent.getStringExtra(EXTRA_SENDER_ID).orEmpty()
        val senderName = intent.getStringExtra(EXTRA_SENDER_NAME).orEmpty()
        val senderImage = intent.getStringExtra(EXTRA_SENDER_IMAGE).orEmpty()

        if (chatRoomId.isBlank() || senderId.isBlank()) return

        val myId = Utils.getUidLoggedIn()
        if (myId.isBlank()) return

        val time = Utils.getTime()

        val db = FirebaseFirestore.getInstance()

        // 1) Save message
        val msgMap = hashMapOf(
            "sender" to myId,
            "receiver" to senderId,
            "message" to replyText,
            "time" to time
        )

        db.collection("Messages")
            .document(chatRoomId)
            .collection("chats")
            .document(time)
            .set(msgMap)

        // 2) Update recent chats for ME (Conversation{myId})
        val recentForMe = hashMapOf(
            "friendid" to senderId,
            "time" to time,
            "sender" to myId,
            "message" to replyText,
            "friendsimage" to senderImage,
            "name" to senderName,
            "person" to "you"
        )

        db.collection("Conversation$myId")
            .document(senderId)
            .set(recentForMe)

        // 3) Update recent chats for OTHER user (Conversation{senderId})
        // Keep it minimal to avoid missing fields breaking your app
        db.collection("Conversation$senderId")
            .document(myId)
            .update(
                mapOf(
                    "message" to replyText,
                    "time" to time,
                    "sender" to myId,
                    "person" to senderName // or my name if you want (from SharedPrefs)
                )
            )
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"

        const val EXTRA_SENDER_ID = "extra_sender_id"
        const val EXTRA_SENDER_NAME = "extra_sender_name"
        const val EXTRA_SENDER_IMAGE = "extra_sender_image"

        const val EXTRA_RECEIVER_ID = "extra_receiver_id" // optional (not required here)
        const val EXTRA_CHATROOM_ID = "extra_chatroom_id"
    }
}