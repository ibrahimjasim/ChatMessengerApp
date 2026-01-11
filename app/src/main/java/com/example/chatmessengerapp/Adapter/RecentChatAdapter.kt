package com.example.chatmessengerapp.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.module.RecentChats
import com.example.chatmessengerapp.module.Users
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class RecentChatAdapter : RecyclerView.Adapter<MyChatListHolder>() {

    var listOfChats = listOf<RecentChats>()
    private var usersList = listOf<Users>()
    var onChatClicked: ((RecentChats) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setUsersList(users: List<Users>) {
        this.usersList = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfChats.size
    }

    @SuppressLint("NotifyDataSetChanged")
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

        chatlist.time?.let {
            val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.timeView.text = simpleDateFormat.format(it)
        }

        // --- START OF THE FINAL FIX ---
        // Instead of using chatlist.status, find the user in our live list
        // and get their real-time status.
        val user = usersList.find { it.userid == chatlist.friendid }

        if (user != null && user.status == "Online") {
            // User is found and their live status is "Online"
            holder.onlineIndicator.setImageResource(R.drawable.onlinestatus)
            holder.onlineIndicator.visibility = View.VISIBLE
        } else {
            // User is not found or their status is "Offline" or something else
            holder.onlineIndicator.setImageResource(R.drawable.offlinestatus)
            // You can choose to either show the red dot or hide it completely
            holder.onlineIndicator.visibility = View.VISIBLE // or View.GONE
        }
        // --- END OF THE FINAL FIX ---

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
    val onlineIndicator: ImageView = itemView.findViewById(R.id.onlineIndicator)
}
