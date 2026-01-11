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
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class RecentChatAdapter :
    RecyclerView.Adapter<RecentChatAdapter.MyChatListHolder>() {

    private var listOfChats = listOf<RecentChats>()
    var onChatClicked: ((RecentChats) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyChatListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int = listOfChats.size

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<RecentChats>) {
        listOfChats = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: MyChatListHolder,
        position: Int
    ) {
        val chat = listOfChats[position]

        holder.userName.text = chat.name

        val msg =
            chat.message?.split(" ")?.take(4)?.joinToString(" ") ?: ""
        holder.lastMessage.text = "${chat.person}: $msg"

        Glide.with(holder.itemView.context)
            .load(chat.friendsimage)
            .into(holder.imageView)

        chat.time?.let {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.timeView.text = sdf.format(it)
        }

        holder.onlineIndicator.visibility = View.GONE
        chat.friendid?.let { uid ->
            observeUserStatus(uid, holder.onlineIndicator)
        }

        holder.itemView.setOnClickListener {
            onChatClicked?.invoke(chat)
        }
    }

    private fun observeUserStatus(
        userId: String,
        statusView: ImageView
    ) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("status")
            .child(userId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)

                if (status == "Online") {
                    statusView.setImageResource(R.drawable.onlinestatus)
                } else {
                    statusView.setImageResource(R.drawable.offlinestatus)
                }
                statusView.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                statusView.setImageResource(R.drawable.offlinestatus)
                statusView.visibility = View.VISIBLE
            }
        })
    }

    // ViewHolder
    inner class MyChatListHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imageView: CircleImageView =
            itemView.findViewById(R.id.recentChatImageView)
        val userName: TextView =
            itemView.findViewById(R.id.recentChatTextName)
        val lastMessage: TextView =
            itemView.findViewById(R.id.recentChatTextLastMessage)
        val timeView: TextView =
            itemView.findViewById(R.id.recentChatTextTime)
        val onlineIndicator: ImageView =
            itemView.findViewById(R.id.onlineIndicator)
    }
}