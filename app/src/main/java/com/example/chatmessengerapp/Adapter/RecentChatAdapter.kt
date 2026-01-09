package com.example.chatmessengerapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.RecentChats
import de.hdodenhof.circleimageview.CircleImageView

class RecentChatAdapter : RecyclerView.Adapter<MyChatListHolder>() {

    var listOfChats = listOf<RecentChats>()
    var onChatClicked: ((RecentChats) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfChats.size
    }

    fun setList(list: List<RecentChats>) {
        this.listOfChats = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyChatListHolder, position: Int) {
        val chatlist = listOfChats[position]

        holder.userName.text = chatlist.name

        val lastMessageText = chatlist.message?.split(" ")?.take(4)?.joinToString(" ") ?: ""
        val fullLastMessage = "${chatlist.person}: $lastMessageText"
        holder.lastMessage.text = fullLastMessage

        Glide.with(holder.itemView.context).load(chatlist.friendsimage).into(holder.imageView)

        holder.timeView.text = Utils.formatTime(chatlist.time)

        holder.itemView.setOnClickListener {
            onChatClicked?.invoke(chatlist)
        }
    }
}

class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
    val userName: TextView = itemView.findViewById(R.id.recentChatTextName)
    val lastMessage: TextView = itemView.findViewById(R.id.recentChatTextLastMessage)
    val timeView: TextView = itemView.findViewById(R.id.recentChatTextTime)
}