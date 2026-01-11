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
import com.google.firebase.database.FirebaseDatabase // IMPORTANT
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

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
        // This makes the status change faster.
        val uid = Utils.getUidLoggedIn()
        if (uid.isNotBlank()) {
            FirebaseDatabase.getInstance().getReference("/status/$uid").setValue("Offline")
        }
    }

    private fun setupPresenceSystem() {
        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) return

        // 1. Create a reference to the Realtime Database for this user's status.
        val userStatusRtdbRef = FirebaseDatabase.getInstance().getReference("/status/$uid")
        // 2. Create a reference to the user's document in Firestore.
        val userStatusFirestoreRef = firestore.collection("Users").document(uid)

        // 3. Listen for the Realtime Database's connection state.
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    // We are connected. Set our RTDB status to "Online".
                    userStatusRtdbRef.setValue("Online")
                    // This is the "last will": if we disconnect unexpectedly, the server will set our status to "Offline".
                    userStatusRtdbRef.onDisconnect().setValue("Offline")
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.w("MainActivity", "RTDB connection listener was cancelled", error.toException())
            }
        })

        // 4. Listen for changes in our Realtime Database status...
        userStatusRtdbRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                // ...and copy the updated status ("Online" or "Offline") to FIRESTORE.
                val status = snapshot.getValue(String::class.java) ?: "Offline"
                userStatusFirestoreRef.update("status", status)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.w("MainActivity", "RTDB status listener was cancelled", error.toException())
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
