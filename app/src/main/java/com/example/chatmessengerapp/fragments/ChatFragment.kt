package com.example.chatmessengerapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.Adapter.MessageAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.databinding.FragmentChatBinding
import com.example.chatmessengerapp.module.Messages
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ChatFragment : Fragment() {

    private lateinit var args: ChatFragmentArgs
    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args = ChatFragmentArgs.fromBundle(requireArguments())
        val user = args.users

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolBarChat)
        val imageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val tvUserName = toolbar.findViewById<TextView>(R.id.chatUserName)
        val backBtn = toolbar.findViewById<ImageView>(R.id.chatBackBtn)


        val tvStatus = view.findViewById<TextView>(R.id.chatUserStatus)

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(requireContext())
            .load(user.imageUrl)
            .placeholder(R.drawable.person)
            .into(imageView)

        tvUserName.text = user.username ?: ""

        // REALTIME STATUS
        val friendId = user.userid ?: return
        observeUserStatus(friendId, tvStatus)

        // RecyclerView
        adapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.messagesRecyclerView.adapter = adapter

        val senderId = Utils.getUidLoggedIn()

        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(
                senderId,
                friendId,
                user.username ?: "",
                user.imageUrl ?: ""
            )
        }

        viewModel.getMessages(friendId).observe(viewLifecycleOwner) { list: List<Messages> ->
            adapter.setList(list)
        }
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