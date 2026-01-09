package com.example.chatmessengerapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.Utils
import com.example.chatmessengerapp.module.Messages

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageHolder>() {

    private var listOfMessage: List<Messages> = emptyList()

    private companion object {
        const val LEFT = 0
        const val RIGHT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)

        val layout = if (viewType == RIGHT) {
            R.layout.chatitemright
        } else {
            R.layout.chatitemleft
        }

        return MessageHolder(inflater.inflate(layout, parent, false))
    }

    override fun getItemCount() = listOfMessage.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        // Check if the message is an image or text
        if (message.imageUrl != null) {
            // It's an image message
            holder.messageText.visibility = View.GONE
            holder.messageImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(message.imageUrl)
                .placeholder(R.drawable.person) // Optional: show a placeholder while loading
                .into(holder.messageImage)
        } else {
            // It's a text message
            holder.messageImage.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
            holder.messageText.text = message.message ?: ""
        }

        val time = message.time.orEmpty()
        holder.timeOfSent.text = if (time.length >= 5) time.take(5) else time
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfMessage[position].sender == Utils.getUidLoggedIn()) RIGHT else LEFT
    }

    fun setList(newList: List<Messages>) {
        listOfMessage = newList
        notifyDataSetChanged()
    }

    class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.show_message)
        val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
        val messageImage: ImageView = itemView.findViewById(R.id.message_image) // New ImageView
    }
}