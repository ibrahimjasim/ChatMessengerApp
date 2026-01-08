package com.example.chatmessengerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var token: String = ""

    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        requestNotificationPermissionIfNeeded()

        generateToken()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_POST_NOTIFICATIONS
            )
        }
    }

    private fun generateToken() {
        // Only generate/save token if user is logged in
        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) return

        FirebaseInstallations.getInstance().id
            .addOnSuccessListener {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { getToken ->
                        token = getToken

                        val map = hashMapOf<String, Any>("token" to token)
                        firestore.collection("Tokens").document(uid).set(map)
                    }
            }
    }

    override fun onStart() {
        super.onStart()
        setUserStatus("Online")
    }

    override fun onResume() {
        super.onResume()
        setUserStatus("Online")
    }

    override fun onPause() {
        super.onPause()
        setUserStatus("Offline")
    }

    private fun setUserStatus(status: String) {
        val uid = Utils.getUidLoggedIn()
        if (uid.isBlank()) return

        firestore.collection("Users").document(uid).update("status", status)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // If we are on Home fragment, exit app, else normal back
        if (navController.currentDestination?.id == R.id.homeFragment) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }
}