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
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserHolder>() {

    private var listOfUsers = listOf<Users>()
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.userlistitem, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int = listOfUsers.size

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = listOfUsers[position]

        holder.profileName.text =
            user.username?.split(" ")?.firstOrNull() ?: ""

        holder.statusImageView.visibility = View.GONE

        user.userid?.let { uid ->
            observeUserStatus(uid, holder.statusImageView)
        }

        Glide.with(holder.itemView.context)
            .load(user.imageUrl)
            .into(holder.imageProfile)

        holder.itemView.setOnClickListener {
            listener?.onUserSelected(position, user)
        }
    }

    private fun observeUserStatus(
        userId: String,
        statusView: ImageView
    ) {
        val statusRef = FirebaseDatabase.getInstance()
            .getReference("status")
            .child(userId)

        statusRef.addValueEventListener(object : ValueEventListener {
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

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<Users>) {
        listOfUsers = list
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // ---------------- VIEW HOLDER ----------------

    inner class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileName: TextView = itemView.findViewById(R.id.userName)
        val imageProfile: CircleImageView = itemView.findViewById(R.id.imageViewUser)
        val statusImageView: ImageView = itemView.findViewById(R.id.statusOnline)
    }
}

// ---------------- CLICK LISTENER ----------------

interface OnItemClickListener {
    fun onUserSelected(position: Int, users: Users)
}