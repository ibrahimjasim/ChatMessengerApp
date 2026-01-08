package com.example.chatmessengerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.chatmessengerapp.R

object NotificationsHelper {

    private const val CHANNEL_ID = "chat_messages"
    private const val CHANNEL_NAME = "Chat messages"

    fun showMessageNotification(
        context: Context,
        chatRoomId: String,
        senderId: String,
        senderName: String,
        senderImage: String,
        messageText: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        val remoteInput = RemoteInput.Builder(NotificationsReply.KEY_TEXT_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, NotificationsReply::class.java).apply {
            putExtra(NotificationsReply.EXTRA_CHATROOM_ID, chatRoomId)
            putExtra(NotificationsReply.EXTRA_SENDER_ID, senderId)
            putExtra(NotificationsReply.EXTRA_SENDER_NAME, senderName)
            putExtra(NotificationsReply.EXTRA_SENDER_IMAGE, senderImage)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            chatRoomId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_launcher_foreground,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName.ifBlank { "New message" })
            .setContentText(messageText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(replyAction)
            .build()

        manager.notify(chatRoomId.hashCode(), notification)
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}