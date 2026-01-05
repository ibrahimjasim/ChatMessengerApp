package com.example.chatmessengerapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatmessengerapp.Adapter.UserAdapter
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.databinding.FragmentHomeBinding
import com.example.chatmessengerapp.mvvm.ChatAppViewModel

class HomeFragment : Fragment() {

    lateinit var useradapter: UserAdapter
    lateinit var userViewModel: ChatAppViewModel
    lateinit var homebinding: FragmentHomeBinding


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

        useradapter = UserAdapter()

        val layoutManager = LinearLayoutManager(activity)
        homebinding.rvUsers.layoutManager = layoutManager

        userViewModel.getUsers().observe(viewLifecycleOwner, Observer {

            useradapter.setList(it)
            homebinding.rvUsers.adapter = useradapter
        })
    }
}
