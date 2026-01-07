ackage com.example.chatmessengerapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatmessengerapp.R
import com.example.chatmessengerapp.module.RecentChats
import de.hdodenhof.circleimageview.CircleImageView

class RecentChatAdapter : RecyclerView.Adapter<MyChatListHolder>() {

    private var listOfChats: List<RecentChats> = emptyList()
    private var listener: onChatClicked? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)
    }

    override fun getItemCount(): Int = listOfChats.size

    fun setList(list: List<RecentChats>) {
        listOfChats = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyChatListHolder, position: Int) {
        val chatlist = listOfChats[position]

        holder.userName.text = chatlist.name ?: ""

        val msg = chatlist.message.orEmpty()
        val shortMsg = msg.split(" ").take(4).joinToString(" ")
        holder.lastMessage.text = "${chatlist.person ?: ""}: $shortMsg"

        Glide.with(holder.itemView.context)
            .load(chatlist.friendsimage)
            .placeholder(R.drawable.person)
            .into(holder.imageView)

        val time = chatlist.time.orEmpty()
        holder.timeView.text = if (time.length >= 5) time.take(5) else time

        holder.itemView.setOnClickListener {
            listener?.getOnChatCLickedItem(position, chatlist)
        }
    }

    fun setOnChatClickListener(listener: onChatClicked) {
        this.listener = listener
    }
}

class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
    val userName: TextView = itemView.findViewById(R.id.recentChatTextName)
    val lastMessage: TextView = itemView.findViewById(R.id.recentChatTextLastMessage)
    val timeView: TextView = itemView.findViewById(R.id.recentChatTextTime)
}

interface onChatClicked {
    fun getOnChatCLickedItem(position: Int, chatList: RecentChats)
}