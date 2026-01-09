package com.example.chatmessengerapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatmessengerapp.MainActivity
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialogSignIn: ProgressDialog
    private lateinit var signInBinding: ActivitySignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signInBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        // IF user has logged in
        // user has to log in only once
        // if (auth.currentUser != null) {
        //  startActivity(Intent(this, MainActivity::class.java))
        //  finish()
        // }

        progressDialogSignIn = ProgressDialog(this)
        progressDialogSignIn.setMessage("Signing In")


        signInBinding.signInTextToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        signInBinding.loginButton.setOnClickListener {

            val email = signInBinding.loginetemail.text.toString().trim()
            val password = signInBinding.loginetpassword.text.toString().trim()

            var isValid = true
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (isValid) {
                signIn(password, email)
            }
        }
    }

    private fun signIn(password: String, email: String) {
        progressDialogSignIn.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (progressDialogSignIn.isShowing) {
                    progressDialogSignIn.dismiss()
                }

                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Authentication failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (progressDialogSignIn.isShowing) {
            progressDialogSignIn.dismiss()
        }
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dismiss dialog to prevent window leaks
        if (progressDialogSignIn.isShowing) {
            progressDialogSignIn.dismiss()
        }
    }
}
