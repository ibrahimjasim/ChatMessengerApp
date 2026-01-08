package com.example.chatmessengerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.chatmessengerapp.MainActivity
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.SharedPrefs
import kotlin.jvm.java
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"


class FirebaseService : FirebaseMessagingService() {
    companion object {
        private const val KEY_REPLY_TEXT = "KEY_REPLY_TEXT"

        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }


    }


    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken


    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            createNotificationChannel(notificationManager)

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)


        // FOR REPLYING TO NOTIFICATION

        val remoteInput = androidx.core.app.RemoteInput.Builder(KEY_REPLY_TEXT).setLabel("Reply").build()
        val replyIntent = Intent(this, NotificationsReply::class.java)



        // For reply action we are alos passing pending intent

        val replyPendingIntent = PendingIntent.getBroadcast(this, 0, replyIntent, PendingIntent.FLAG_MUTABLE)
        val replyAction = NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent).addRemoteInput(remoteInput).build()


        val sharedCustomePref = SharedPrefs(applicationContext)
        // If we have the notifacation id then we can use taht id and send reply
        sharedCustomePref.setIntValue("values", notificationId)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID).setContentText(Html.("<b>${message.data["title"]}</b>: ${message.data["message"]}")).
        setSmallIcon(R.drawable.ic_launcher_foreground).setAutoCancel(true).setContentIntent(pendingIntent).addAction(replyAction).build()



        notificationManager.notify(notificationId, notification)























    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {

        val channelName = "channelName"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = R.color.teal_700

        }

    }
}