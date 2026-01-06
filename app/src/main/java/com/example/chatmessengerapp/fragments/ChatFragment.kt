package com.example.chatmessengerapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

    private val args: ChatFragmentArgs by navArgs()
    private lateinit var chatbinding: FragmentChatBinding
    private lateinit var chatAppViewModel: ChatAppViewModel
    private lateinit var chattoolbar: Toolbar

    private lateinit var circleImageView: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var backbtn: ImageView
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatbinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return chatbinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAppViewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]

        chattoolbar = view.findViewById(R.id.toolBarChat)
        circleImageView = chattoolbar.findViewById(R.id.chatImageViewUser)
        tvStatus = chattoolbar.findViewById(R.id.chatUserStatus)
        tvUserName = chattoolbar.findViewById(R.id.chatUserName)
        backbtn = chattoolbar.findViewById(R.id.chatBackBtn)

        backbtn.setOnClickListener {
            findNavController().popBackStack()
        }

        val user = args.users
        Glide.with(requireContext()).load(user.imageUrl).into(circleImageView)
        tvStatus.text = user.status
        tvUserName.text = user.username

        chatbinding.viewModel = chatAppViewModel
        chatbinding.lifecycleOwner = viewLifecycleOwner

        chatbinding.sendBtn.setOnClickListener {
            chatAppViewModel.sendMessage(
                Utils.getUiLogged(),
                user.userid!!,
                user.username!!,
                user.imageUrl!!
            )
        }

        chatAppViewModel.getMessages(user.userid!!).observe(viewLifecycleOwner) {
            initRecyclerView(it)
        }
    }

    private fun initRecyclerView(list: List<Messages>) {
        messageAdapter = MessageAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        chatbinding.messagesRecyclerView.layoutManager = layoutManager
        messageAdapter.setList(list)
        chatbinding.messagesRecyclerView.adapter = messageAdapter
    }
}
