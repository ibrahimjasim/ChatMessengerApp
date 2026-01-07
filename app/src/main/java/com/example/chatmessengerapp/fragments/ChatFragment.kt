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
import de.hdodenhof.circleimageview.CircleImageView

class ChatFragment : Fragment() {

    private lateinit var args: ChatFragmentArgs
    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args = ChatFragmentArgs.fromBundle(requireArguments())

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val user = args.users

        // Toolbar views
        val toolbar = view.findViewById<Toolbar>(R.id.toolBarChat)
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val tvUserName = toolbar.findViewById<TextView>(R.id.chatUserName)
        val backBtn = toolbar.findViewById<ImageView>(R.id.chatBackBtn)

        // Status is OUTSIDE toolbar in your XML
        val tvStatus = view.findViewById<TextView>(R.id.chatUserStatus)

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(requireContext())
            .load(user.imageUrl)
            .placeholder(R.drawable.person)
            .into(circleImageView)

        tvUserName.text = user.username ?: ""
        tvStatus.text = user.status ?: ""

        // RecyclerView (id is messagesRecyclerView)
        adapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.messagesRecyclerView.adapter = adapter

        // Send
        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(
                Utils.getUidLoggedIn(),
                user.userid ?: return@setOnClickListener,
                user.username ?: "",
                user.imageUrl ?: ""
            )
        }

        // Observe messages
        val friendId = user.userid ?: return
        viewModel.getMessages(friendId).observe(viewLifecycleOwner) { list: List<Messages> ->
            adapter.setList(list)
        }
    }
}