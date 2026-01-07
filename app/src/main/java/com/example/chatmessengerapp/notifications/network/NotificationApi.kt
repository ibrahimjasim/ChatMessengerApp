package com.example.chatmessengerapp.notifications.network

import com.example.chatmessengerapp.notifications.Constants.Companion.CONTENT_TYPE
import com.example.chatmessengerapp.notifications.Constants.Companion.SERVER_KEY
import com.example.chatmessengerapp.notifications.entity.PushNotification
import com.google.android.gms.common.api.Response

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>


}