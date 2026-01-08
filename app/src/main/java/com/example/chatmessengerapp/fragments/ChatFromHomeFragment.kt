package com.example.chatmessengerapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val textViewName = toolbar.findViewById<TextView>(R.id.chatUserName)

        Glide.with(requireContext())
            .load(recent.friendsimage)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(circleImageView)

        textViewName.text = recent.name ?: ""

        // Back
        binding.chatBackBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Send
        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(
                Utils.getUidLoggedIn(),
                recent.friendid ?: return@setOnClickListener,
                recent.name ?: "",
                recent.friendsimage ?: ""
            )
        }

        // Messages
        val friendId = recent.friendid ?: return
        viewModel.getMessages(friendId).observe(viewLifecycleOwner, Observer { list ->
            initRecyclerView(list)
        })
    }

    private fun initRecyclerView(list: List<Messages>) {
        if (!::adapter.isInitialized) {
            adapter = MessageAdapter()
            binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            binding.messagesRecyclerView.adapter = adapter
        }

        adapter.setList(list)
    }
}