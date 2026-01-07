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
import com.example.chatmessengerapp.Adapter.OnChatClickedListener
import com.example.chatmessengerapp.Adapter.RecentChatAdapter
import com.example.chatmessengerapp.Adapter.UserAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.activities.SignInActivity
import com.example.chatmessengerapp.databinding.FragmentHomeBinding
import com.example.chatmessengerapp.module.RecentChats
import com.example.chatmessengerapp.module.Users
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.example.chatmessengerapp.mvvm.RecentChats
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment(), OnItemClickListener, OnChatClickedListener {

    private lateinit var rvUsers: RecyclerView
    private lateinit var rvRecentChats: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var recentAdapter: RecentChatAdapter
    private lateinit var viewModel: ChatAppViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var circleImageView: CircleImageView
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner

        // Toolbar views (from fragment_home.xml)
        toolbar = view.findViewById(R.id.toolbarMain)
        val logoutImage = toolbar.findViewById<ImageView>(R.id.logOut)
        circleImageView = toolbar.findViewById(R.id.tlImage)

        // Observe profile image
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

        rvUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentChats.layoutManager = LinearLayoutManager(requireContext())

        rvUsers.adapter = adapter
        rvRecentChats.adapter = recentAdapter

        adapter.setOnClickListener(this)
        recentAdapter.setOnChatClickListener(this)

        // Load users list
        viewModel.getUsers().observe(viewLifecycleOwner, Observer { users ->
            adapter.setList(users)
        })

        // Load recent chats list
        viewModel.getRecentUsers().observe(viewLifecycleOwner, Observer { chats ->
            recentAdapter.setList(chats)
        })

        // Go to settings
        circleImageView.setOnClickListener {
            // ✅ Your nav_graph uses settingsFragment (not settingFragment)
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    override fun onUserSelected(position: Int, users: Users) {
        val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(users)
        findNavController().navigate(action)
    }

    override fun onChatClicked(position: Int, chatList: RecentChats) {
        // ✅ Your nav_graph action is action_homeFragment_to_chatFromHomeFragment
        val action = HomeFragmentDirections.actionHomeFragmentToChatFromHomeFragment(chatList)
        findNavController().navigate(action)
    }
}