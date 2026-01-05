package com.example.chatmessengerapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatmessengerapp.Adapter.UserAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.FragmentHomeBinding
import com.example.chatmessengerapp.mvvm.ChatAppViewModel
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment(), OnUserClickListener {

    lateinit var useradapter: UserAdapter
    lateinit var userViewModel: ChatAppViewModel
    lateinit var homebinding: FragmentHomeBinding
    lateinit var fbauth: FirebaseAuth
    lateinit var toolbar : Toolbar
    lateinit var circleImageView : CircleImageView
    lateinit var rvUsers : RecyclerView





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homebinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return homebinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)

        fbauth = FirebaseAuth.getInstance()

        toolbar = view.findViewById(R.id.toolbarMain)
        circleImageView = toolbar.findViewById(R.id.tlImage)

        homebinding.lifecycleOwner = viewLifecycleOwner

        useradapter = UserAdapter()

        val layoutManager = LinearLayoutManager(activity)
        homebinding.rvUsers.layoutManager = layoutManager

        userViewModel.getUsers().observe(viewLifecycleOwner, Observer {

            useradapter.setUserList(it)
            useradapter.setOnUserClickListener(this)
            rvUsers.adapter = useradapter
        })

        homebinding.logOut.setOnClickListener {
            fbauth.signOut()
    }


        userViewModel.imageUrl.observe(viewLifecycleOwner, Observer {

            Glide.with(requireContext()).load(it).into(circleImageView)


        })
        }
}
