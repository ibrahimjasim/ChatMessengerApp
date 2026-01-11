package com.example.chatmessengerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.chatmessengerapp.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        requestNotificationPermissionIfNeeded()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            goToSignInActivity()
            return
        }
        // Set up the reliable presence system
        setupPresenceSystem()
    }

    override fun onStop() {
        super.onStop()
        // When the app goes into the background gracefully, tell the Realtime Database we are offline.
        val uid = Utils.getUidLoggedIn()
        if (uid.isNotBlank()) {
            FirebaseDatabase.getInstance().getReference("/status/$uid").setValue("Offline")
        }
    }

    private fun setupPresenceSystem() {
        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) return

        val userStatusRtdbRef = FirebaseDatabase.getInstance().getReference("/status/$uid")
        val userStatusFirestoreRef = firestore.collection("Users").document(uid)

        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRtdbRef.setValue("Online")
                    userStatusRtdbRef.onDisconnect().setValue("Offline")

                    // *** ADDED: Write status to Firestore ***
                    userStatusFirestoreRef.update("status", "Online")

                } else {
                    // *** ADDED: Handle graceful offline status in Firestore ***
                    userStatusFirestoreRef.update("status", "Offline")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "RTDB connection listener was cancelled", error.toException())
            }
        })

        // *** ADDED: Listen to RTDB status and update Firestore accordingly ***
        userStatusRtdbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status != null) {
                    userStatusFirestoreRef.update("status", status)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Not strictly required to handle this, but good practice
            }

        })
    }

    private fun goToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.homeFragment) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }
}