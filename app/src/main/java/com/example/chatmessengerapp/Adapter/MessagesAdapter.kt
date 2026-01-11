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
import java.text.SimpleDateFormat
import java.util.Locale


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

        // Handle Image vs Text
        if (message.imageUrl != null && message.imageUrl.isNotBlank()) {
            // Image message
            holder.messageText.visibility = View.GONE
            holder.image.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(message.imageUrl).into(holder.image)
        } else {
            // Text message
            holder.image.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
            holder.messageText.text = message.message ?: ""
        }

        val timeValue = message.time // This is of type Any?

        if (timeValue is com.google.firebase.Timestamp) {
            // If it's a Firestore Timestamp, convert it to a Date
            val date = timeValue.toDate()
            val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.timeOfSent.text = simpleDateFormat.format(date)
        } else if (timeValue is java.util.Date) {
            // If it's already a Date, just format it
            val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.timeOfSent.text = simpleDateFormat.format(timeValue)
        } else if (timeValue is String) {
            // As a fallback for old data, just display the first 5 chars
            holder.timeOfSent.text = timeValue.take(5)
        }
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
        val image: ImageView = itemView.findViewById(R.id.message_image)
    }
}