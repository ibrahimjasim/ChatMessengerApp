package com.example.chatmessengerapp.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.chatmessengerapp.module.Users
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatFragment : Fragment() {

    private lateinit var args: ChatFragmentArgs
    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var adapter: MessageAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                sendImage(imageUri)
            }
        }
    }

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
        val user = args.users

        // Critical check: If friend's ID is missing, we cannot proceed.
        val friendId = user.userid
        if (friendId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Error: User ID is missing.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar setup
        val toolbar = view.findViewById<Toolbar>(R.id.toolBarChat)
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val tvUserName = toolbar.findViewById<TextView>(R.id.chatUserName)
        val backBtn = toolbar.findViewById<ImageView>(R.id.chatBackBtn)

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(requireContext())
            .load(user.imageUrl)
            .placeholder(R.drawable.person)
            .into(circleImageView)

        tvUserName.text = user.username ?: ""
        view.findViewById<TextView>(R.id.chatUserStatus).text = user.status ?: ""

        // RecyclerView setup
        adapter = MessageAdapter()
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.messagesRecyclerView.adapter = adapter

        // Send Text Button
        binding.sendBtn.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendTextMessage(
                    Utils.getUidLoggedIn(),
                    friendId, // Safe to use friendId here
                    user.username ?: "",
                    user.imageUrl ?: "",
                    messageText
                )
            }
        }

        // Attach Image Button
        binding.attachBtn.setOnClickListener {
            openGallery()
        }

        // Observe messages
        viewModel.getMessages(friendId).observe(viewLifecycleOwner) { list: List<Messages> ->
            adapter.setList(list)
            binding.messagesRecyclerView.scrollToPosition(list.size - 1)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun sendImage(imageUri: Uri) {
        val user = args.users
        val friendId = user.userid
        if (friendId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Error: Cannot send image. User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.sendImageMessage(
            Utils.getUidLoggedIn(),
            friendId,
            user.username ?: "",
            user.imageUrl ?: "",
            imageUri
        )
        Toast.makeText(requireContext(), "Sending image...", Toast.LENGTH_SHORT).show()
    }
}
