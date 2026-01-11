package com.example.chatmessengerapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.Adapter.MessageAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.databinding.FragmentChatfromHomeBinding
import com.example.chatmessengerapp.module.Messages
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ChatFromHomeFragment : Fragment() {

    private val args: ChatFromHomeFragmentArgs by navArgs()

    private lateinit var binding: FragmentChatfromHomeBinding
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatfrom_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recent = args.recentchats

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolBarChat)
        val imageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val tvUserName = toolbar.findViewById<TextView>(R.id.chatUserName)

        // Status TextView
        val tvStatus = view.findViewById<TextView>(R.id.chatUserStatus)

        Glide.with(requireContext())
            .load(recent.friendsimage)
            .placeholder(R.drawable.person)
            .into(imageView)

        tvUserName.text = recent.name ?: ""

        // REALTIME STATUS
        val friendId = recent.friendid ?: return
        observeUserStatus(friendId, tvStatus)

        // Back
        binding.chatBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Send
        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(
                Utils.getUidLoggedIn(),
                friendId,
                recent.name ?: "",
                recent.friendsimage ?: ""
            )
        }

        // Messages
        viewModel.getMessages(friendId).observe(viewLifecycleOwner) { list ->
            initRecyclerView(list)
        }
    }

    private fun initRecyclerView(list: List<Messages>) {
        if (!::adapter.isInitialized) {
            adapter = MessageAdapter()
            binding.messagesRecyclerView.layoutManager =
                LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            binding.messagesRecyclerView.adapter = adapter
        }
        adapter.setList(list)
    }

    // ---------------- REALTIME DATABASE STATUS ----------------

    private fun observeUserStatus(
        userId: String,
        statusTextView: TextView
    ) {
        val statusRef = FirebaseDatabase.getInstance()
            .getReference("status")
            .child(userId)

        statusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                statusTextView.text =
                    if (status == "Online") "Online" else "Offline"
            }

            override fun onCancelled(error: DatabaseError) {
                statusTextView.text = "Offline"
            }
        })
    }
}