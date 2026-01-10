package com.example.chatmessengerapp.fragments

import android.content.Intent
import android.os.Bundle
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar
        toolbar = view.findViewById(R.id.toolbarMain)
        val logoutImage = toolbar.findViewById<ImageView>(R.id.logOut)
        circleImageView = toolbar.findViewById(R.id.tlImage)

        // Profile image
        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { url ->
            if (!url.isNullOrBlank()) {
                Glide.with(requireContext()).load(url).into(circleImageView)
            }
        })

        // Logout
        val firebaseAuth = FirebaseAuth.getInstance()
        logoutImage.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            requireActivity().finish()
        }

        // RecyclerViews
        rvUsers = view.findViewById(R.id.rvUsers)
        rvRecentChats = view.findViewById(R.id.rvRecentChats)

        adapter = UserAdapter()
        recentAdapter = RecentChatAdapter()

        rvUsers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentChats.layoutManager = LinearLayoutManager(requireContext())

        rvUsers.adapter = adapter
        rvRecentChats.adapter = recentAdapter

        adapter.setOnClickListener(this)
        recentAdapter.onChatClicked = {
            val action = HomeFragmentDirections.actionHomeFragmentToChatFromHomeFragment(it)
            findNavController().navigate(action)
        }

        // Load users
        viewModel.getUsers().observe(viewLifecycleOwner, Observer { users ->
            adapter.setList(users)
        })

        // Load recent chats
        viewModel.getRecentUsers().observe(viewLifecycleOwner, Observer { chats ->
            recentAdapter.setList(chats)
        })

        // Go to settings
        circleImageView.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
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

    private fun startConversationNotificationListener() {
        if (convoListener != null) return // already listening

        val myId = Utils.getUidLoggedIn()
        if (myId.isBlank()) return

        convoListener = FirebaseFirestore.getInstance()
            .collection("Conversation$myId")
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                // Only react to latest doc change
                val change = snapshot.documentChanges.firstOrNull() ?: return@addSnapshotListener
                val doc = change.document

                val sender = doc.getString("sender").orEmpty()
                val friendId = doc.getString("friendid").orEmpty()
                val msg = doc.getString("message").orEmpty()
                val name = doc.getString("name").orEmpty()
                val image = doc.getString("friendsimage").orEmpty()

                // Prevent notifying for my own messages
                if (sender == myId) return@addSnapshotListener

                // Prevent duplicate notification for same message/time
                val time = doc.getTimestamp("time")
                if (time == null) { // This might happen with very old data, skip it.
                    return@addSnapshotListener
                }
                val messageId = doc.id + "_" + time.seconds
                if (messageId == lastNotifiedMessageId) return@addSnapshotListener
                lastNotifiedMessageId = messageId

                val chatRoomId = listOf(myId, friendId).sorted().joinToString("")

                NotificationsHelper.showMessageNotification(
                    context = requireContext(),
                    chatRoomId = chatRoomId,
                    senderId = friendId,
                    senderName = name,
                    senderImage = image,
                    messageText = msg
                )
            }
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
