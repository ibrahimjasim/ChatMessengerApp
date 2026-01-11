package com.example.chatmessengerapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.Adapter.OnItemClickListener
import com.example.chatmessengerapp.Adapter.RecentChatAdapter
import com.example.chatmessengerapp.Adapter.UserAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.activities.SignInActivity
import com.example.chatmessengerapp.databinding.FragmentHomeBinding
import com.example.chatmessengerapp.module.RecentChats
import com.example.chatmessengerapp.module.Users
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.example.chatmessengerapp.notifications.NotificationsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvUsers: RecyclerView
    private lateinit var rvRecentChats: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var recentAdapter: RecentChatAdapter
    private lateinit var viewModel: ChatAppViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var circleImageView: CircleImageView

    private var convoListener: ListenerRegistration? = null
    private var lastNotifiedMessageId: String? = null // prevents duplicates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    // In HomeFragment.kt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar setup
        toolbar = view.findViewById(R.id.toolbarMain)
        val logoutImage = toolbar.findViewById<ImageView>(R.id.logOut)
        circleImageView = toolbar.findViewById(R.id.tlImage)
        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(requireContext()).load(url).into(circleImageView)
            }
        }
        logoutImage.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            requireActivity().finish()
        }
        circleImageView.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        // RecyclerViews setup
        rvUsers = view.findViewById(R.id.rvUsers)
        rvRecentChats = view.findViewById(R.id.rvRecentChats)

        adapter = UserAdapter()
        recentAdapter = RecentChatAdapter()

        rvUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentChats.layoutManager = LinearLayoutManager(requireContext())

        rvUsers.adapter = adapter
        rvRecentChats.adapter = recentAdapter

        adapter.setOnClickListener(this)
        recentAdapter.onChatClicked = {
            val action = HomeFragmentDirections.actionHomeFragmentToChatFromHomeFragment(it)
            findNavController().navigate(action)
        }

        // --- THIS IS THE KEY PART ---
        // Observe the live list of all users from the ViewModel.
        viewModel.getUsers().observe(viewLifecycleOwner) { users ->
            // Pass the live list to BOTH adapters.
            adapter.setList(users)

        }

        viewModel.getRecentUsers().observe(viewLifecycleOwner) { chats ->
            recentAdapter.setList(chats)
        }
    }


    /**
     * Phase 1 notifications:
     * Start listener when fragment becomes visible (prevents duplicate listeners).
     */
    override fun onStart() {
        super.onStart()
        startConversationNotificationListener()
    }

    override fun onStop() {
        super.onStop()
        stopConversationNotificationListener()
    }

    // In HomeFragment.kt, replace the entire function

    private fun startConversationNotificationListener() {
        if (convoListener != null) {
            return // Already listening
        }

        val myId = Utils.getUidLoggedIn()
        if (myId.isBlank()) {
            return
        }

        // --- START OF THE CORRECT AND SAFE FIX ---
        // This query listens to all subcollections named "chats" for new messages.
        convoListener = FirebaseFirestore.getInstance()
            .collectionGroup("Messages")
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // This error will appear in the log if the required index is missing.
                    Log.w("HomeFragment", "CollectionGroup listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    return@addSnapshotListener
                }

                val docChange = snapshot.documentChanges.firstOrNull()
                // We only care about brand new messages for notifications.
                if (docChange?.type != com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    return@addSnapshotListener
                }

                val doc = docChange.document

                // Now, get the data from the message document itself
                val senderId = doc.getString("sender").orEmpty()
                val receiverId = doc.getString("receiver").orEmpty()
                val msg = doc.getString("message").orEmpty()

                // IMPORTANT: Only show a notification if the message is FOR ME, and not FROM ME.
                if (receiverId != myId || senderId == myId) {
                    return@addSnapshotListener
                }

                // We need the sender's name and image for the notification.
                // We fetch this from the "Users" collection.
                FirebaseFirestore.getInstance().collection("Users").document(senderId).get()
                    .addOnSuccessListener { userSnapshot ->
                        if (!userSnapshot.exists()) {
                            return@addOnSuccessListener
                        }

                        val senderName = userSnapshot.getString("username").orEmpty()
                        val senderImage = userSnapshot.getString("imageUrl").orEmpty()

                        // Use the document ID and timestamp to prevent showing the same notification twice
                        val time = doc.getTimestamp("time")
                        val messageId = doc.id + "_" + (time?.seconds ?: 0L)
                        if (messageId == lastNotifiedMessageId) {
                            return@addOnSuccessListener
                        }
                        lastNotifiedMessageId = messageId

                        // Create the chat room ID just like everywhere else to open the correct chat
                        val chatRoomId = listOf(myId, senderId).sorted().joinToString("")

                        // We have everything we need. Show the notification.
                        NotificationsHelper.showMessageNotification(
                            context = requireContext(),
                            chatRoomId = chatRoomId,
                            senderId = senderId,
                            senderName = senderName,
                            senderImage = senderImage,
                            messageText = msg
                        )
                    }
            }
        // --- END OF THE CORRECT AND SAFE FIX ---
    }



    private fun stopConversationNotificationListener() {
        convoListener?.remove()
        convoListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopConversationNotificationListener()
        _binding = null
    }

    override fun onUserSelected(position: Int, users: Users) {
        val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(users)
        findNavController().navigate(action)
    }
}
