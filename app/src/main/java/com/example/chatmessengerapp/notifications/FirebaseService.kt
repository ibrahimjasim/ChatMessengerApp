package com.example.chatmessengerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.chatmessengerapp.MainActivity
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) return

        FirebaseFirestore.getInstance()
            .collection("Tokens")
            .document(uid)
            .set(mapOf("token" to token))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        if (data.isEmpty()) return

        // Expecting DATA payload
        val senderId = data["senderId"].orEmpty()
        val senderName = data["senderName"].orEmpty()
        val senderImage = data["senderImage"].orEmpty()
        val receiverId = data["receiverId"].orEmpty()
        val text = data["message"].orEmpty()

        if (senderId.isBlank() || receiverId.isBlank() || text.isBlank()) return

        // ✅ Always compute chatRoomId exactly like your app does (sorted IDs)
        val chatRoomId = listOf(senderId, receiverId).sorted().joinToString("")

        showMessageNotification(
            senderId = senderId,
            senderName = senderName,
            senderImage = senderImage,
            receiverId = receiverId,
            chatRoomId = chatRoomId,
            messageText = text
        )
    }

    private fun showMessageNotification(
        senderId: String,
        senderName: String,
        senderImage: String,
        receiverId: String,
        chatRoomId: String,
        messageText: String
    ) {
        val channelId = Utils.CHANNEL_ID
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // ✅ Tap notification -> open app (MainActivity)
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            chatRoomId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Inline reply
        val remoteInput = RemoteInput.Builder(NotificationsReply.KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(this, NotificationsReply::class.java).apply {
            putExtra(NotificationsReply.EXTRA_SENDER_ID, senderId)
            putExtra(NotificationsReply.EXTRA_SENDER_NAME, senderName)
            putExtra(NotificationsReply.EXTRA_SENDER_IMAGE, senderImage)
            putExtra(NotificationsReply.EXTRA_RECEIVER_ID, receiverId)
            putExtra(NotificationsReply.EXTRA_CHATROOM_ID, chatRoomId)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            this,
            chatRoomId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground, // replace with your own icon if you have
            "Reply",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with app icon if you want
            .setContentTitle(senderName.ifBlank { "New message" })
            .setContentText(messageText)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(replyAction)
            .build()

        manager.notify(chatRoomId.hashCode(), notification)
    }
}