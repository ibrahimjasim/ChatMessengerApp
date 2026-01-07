package com.example.chatmessengerapp.notifications.network

class RetrofitInstance {

    companion object{

        private val retorfit by lazy {
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()

        }



        }

    val api by lazy {
        retorfit.create(NotificationApi::class.java)
    }
    }
}