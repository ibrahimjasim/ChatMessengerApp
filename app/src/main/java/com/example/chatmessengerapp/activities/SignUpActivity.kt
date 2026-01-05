package com.example.chatmessengerapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatmessengerapp.MainActivity
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {


    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        signUpBinding.signUpTextToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }


        signUpBinding.signUpBtn.setOnClickListener {
            val name = signUpBinding.signUpEtName.text.toString()
            val email = signUpBinding.signUpEmail.text.toString()
            val password = signUpBinding.signUpPassword.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "Email can't be empty", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Password can't be empty", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    signUpUser(name, email, password)
                }
            }
        }
    }

    private fun signUpUser(name: String, email: String, password: String) {
        // Show the progress bar
        signUpBinding.signUpProgressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user!!.uid

                    val userMap = hashMapOf(
                        "userid" to uid,
                        "username" to name,
                        "useremail" to email,
                        "status" to "Online", // Set a default status
                        "imageUrl" to "https://www.pngarts.com/files/6/User-Avatar-in-Suit-PNG.png"
                    )

                    firestore.collection("Users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            // Hide the progress bar
                            signUpBinding.signUpProgressBar.visibility = View.GONE
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            // Redirect to MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Hide the progress bar
                            signUpBinding.signUpProgressBar.visibility = View.GONE
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Hide the progress bar
                    signUpBinding.signUpProgressBar.visibility = View.GONE
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
