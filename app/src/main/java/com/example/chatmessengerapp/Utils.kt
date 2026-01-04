package com.example.chatmessengerapp

class Utils {

    companion object{

        private val auth = FirebaseAuth.getInstance()
        private val userid : String =""

        fun getUiLoggedIn(): String {

            if (auth.currentUser != null){

                userid = auth.currentUser!!.uid

            }

            return userid

        }



    }
}