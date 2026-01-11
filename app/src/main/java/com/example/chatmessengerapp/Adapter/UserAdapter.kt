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
import com.example.chatmessengerapp.module.Users
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter : RecyclerView.Adapter<UserHolder>() {

    private var listOfUsers = listOf<Users>()
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userlistitem, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfUsers.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = listOfUsers[position]

        // Use 'user' instead of 'users' for clarity
        val name = user.username?.split("\\s".toRegex())?.get(0) ?: ""
        holder.profileName.text = name

        // --- THIS IS THE FINAL LOGIC FIX ---
        // Check the status field from the live user data.
        if (user.status == "Online") {
            // If the status is exactly "Online", show the green dot.
            holder.statusImageView.setImageResource(R.drawable.onlinestatus)
            holder.statusImageView.visibility = View.VISIBLE
        } else {
            // For any other status ("Offline", null, blank), show the red dot.
            holder.statusImageView.setImageResource(R.drawable.offlinestatus)
            holder.statusImageView.visibility = View.VISIBLE
        }
        // --- END OF FIX ---

        Glide.with(holder.itemView.context).load(user.imageUrl).into(holder.imageProfile)

        holder.itemView.setOnClickListener {
            listener?.onUserSelected(position, user)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<Users>){
        this.listOfUsers = list
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
}

class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val profileName: TextView = itemView.findViewById(R.id.userName)
    val imageProfile: CircleImageView = itemView.findViewById(R.id.imageViewUser)
    val statusImageView: ImageView = itemView.findViewById(R.id.statusOnline)
}

interface OnItemClickListener {
    fun onUserSelected(position: Int, users: Users)
}
