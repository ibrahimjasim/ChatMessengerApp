package com.example.chatmessengerapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.Adapter.OnItemClickListener
import com.example.chatmessengerapp.Adapter.UserAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.activities.SignInActivity
import com.example.chatmessengerapp.databinding.FragmentHomeBinding
import com.example.chatmessengerapp.module.Users
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment(), OnItemClickListener {

    private lateinit var useradapter: UserAdapter
    private lateinit var userViewModel: ChatAppViewModel
    private lateinit var homebinding: FragmentHomeBinding
    private lateinit var fbauth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homebinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return homebinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
        fbauth = FirebaseAuth.getInstance()
        useradapter = UserAdapter()

        // Setup recycler view
        homebinding.rvUsers.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = useradapter
        }

        // Set the listener on the adapter
        useradapter.setOnClickListener(this)

        userViewModel.getUsers().observe(viewLifecycleOwner, Observer {
            // Use correct method name
            useradapter.setList(it)
        })

        homebinding.logOut.setOnClickListener {
            fbauth.signOut()
            // After logout, go to sign in screen
            val intent = Intent(activity, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        userViewModel.imageUrl.observe(viewLifecycleOwner, Observer {
            // Use binding to access views
            Glide.with(requireContext()).load(it).into(homebinding.tlImage)
        })
    }

    override fun onUserSelected(position: Int, users: Users) {
        // TODO: Handle user click to navigate to chat screen
    }
}
