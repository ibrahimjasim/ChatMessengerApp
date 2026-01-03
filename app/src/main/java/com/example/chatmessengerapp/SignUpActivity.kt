@file:Suppress("DEPRECATION")

package com.example.chatmessengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.to

class SignUpActivity : AppCompatActivity() {


    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var firestore: FirebaseFirestore           //Or any other storage service//
    private lateinit var auth: FirebaseAuth
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var signUpPd: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        signUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        signUpPd = ProgressDialog(this)

        signUpBinding.signUpTextToSignIn.setOnClickListener {

            startActivity(Intent(this, SignInActivity::class.java))


        }


        signUpBinding.signUpBtn.setOnClickListener {

            name = signUpBinding.signUpEtName.text.toString()
            email = signUpBinding.signUpEmail.text.toString()
            password = signUpBinding.signUpPassword.text.toString()


            if (signUpBinding.signUpEtName.text.isEmpty()) {

                Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show()


            }

            if (signUpBinding.signUpPassword.text.isEmpty()) {

                Toast.makeText(this, "Password can't be empty", Toast.LENGTH_SHORT).show()


            }

            if (signUpBinding.signUpEmail.text.isEmpty()) {

                Toast.makeText(this, "Email can't be empty", Toast.LENGTH_SHORT).show()


            }

            if (signUpBinding.signUpEtName.text.isNotEmpty() &&
                signUpBinding.signUpEmail.text.isNotEmpty() &&
                signUpBinding.signUpPassword.text.isNotEmpty()
            ) {

                signUpUser(name, email, password)


            }

        }


    }

    private fun signUpUser(name: String, email: String, password: String) {

        signUpPd.show()
        signUpPd.setMessage("Signing Up")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {

                if (it.isSuccessful) {

                    val user = auth.currentUser


                    val hashMap = hashMapOf("userid" to user!!.uid,
                        "username" to name,
                        "useremail" to email,
                        "status" to "default",
                        "imageUrl" to "https://www.pngarts.com/files/6/User-Avatar-in-Suit-PNG.png")


                    firestore.collection("users").document(user.uid).set(hashMap)
                    signUpPd.dismiss()



                }




            }
    }
}