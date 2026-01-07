package com.example.chatmessengerapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.module.RecentChats
import de.hdodenhof.circleimageview.CircleImageView

class RecentChatAdapter : RecyclerView.Adapter<RecentChatAdapter.MyChatListHolder>() {

    private var listOfChats: List<RecentChats> = emptyList()
    private var listener: OnChatClicked? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int = listOfChats.size

    override fun onBindViewHolder(holder: MyChatListHolder, position: Int) {
        val chat = listOfChats[position]

        holder.userName.text = chat.name.orEmpty()

        val msg = chat.message.orEmpty()
        val shortMsg = msg.split(" ").take(4).joinToString(" ")
        holder.lastMessage.text = "${chat.person.orEmpty()}: $shortMsg"

        Glide.with(holder.itemView.context)
            .load(chat.friendsimage)
            .placeholder(R.drawable.person)
            .into(holder.imageView)

        val time = chat.time.orEmpty()
        holder.timeView.text = if (time.length >= 5) time.take(5) else time

        holder.itemView.setOnClickListener {
            listener?.getOnChatClickedItem(position, chat)
        }
    }

    fun setList(list: List<RecentChats>) {
        listOfChats = list
        notifyDataSetChanged()
    }

    fun setOnChatClickListener(listener: OnChatClicked) {
        this.listener = listener
    }

    class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
        val userName: TextView = itemView.findViewById(R.id.recentChatTextName)
        val lastMessage: TextView = itemView.findViewById(R.id.recentChatTextLastMessage)
        val timeView: TextView = itemView.findViewById(R.id.recentChatTextTime)
    }

    interface OnChatClicked {
        fun getOnChatClickedItem(position: Int, chatList: RecentChats)
    }
}