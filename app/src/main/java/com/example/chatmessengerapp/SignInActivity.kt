@file:Suppress("DEPRECATION")

package com.example.chatmessengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class SignInActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialogSignIn: ProgressDialog
    private lateinit var signInBinding: ActivitySignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signInBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        // IF user has logged in
        //user has to log in only once

        //if (auth.currentUser != null) {

        //  startActivity(Intent(this, MainActivity::class.java))


        //}

        progressDialogSignIn = ProgressDialog(this)

        signInBinding.signInTextToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        signInBinding.loginButton.setOnClickListener {

            email = signInBinding.loginetemail.text.toString()
            password = signInBinding.loginetpassword.text.toString()

            if (signInBinding.loginetemail.text.isEmpty()) {

                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()

            }

            if (signInBinding.loginetpassword.text.isEmpty()) {

                Toast.makeText(this, "Enter PAssword", Toast.LENGTH_SHORT).show()

            }

            if (signInBinding.loginetemail.text.isNotEmpty() && signInBinding.loginetpassword.text.isNotEmpty()) {

                signIn(password, email)

            }


        }


    }

    private fun signIn(password: String, email: String) {

        progressDialogSignIn.show()
        progressDialogSignIn.setMessage("Signing In")


        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {

                if (it.isSuccessful) {

                    progressDialogSignIn.dismiss()

                    startActivity(Intent(this, MainActivity::class.java))

                } else {
                    progressDialogSignIn.dismiss()
                    Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show()


                }


            }.addOnFailureListener { exception ->


                when (exception) {

                    is FirebaseAuthInvalidCredentialsException->{
                        Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show()


                    } else->{

                    //other Exceptions
                    Toast.makeText(this,"Auth failed", Toast.LENGTH_SHORT).show()


                }

                }






            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        progressDialogSignIn.dismiss()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialogSignIn.dismiss()
    }


}


