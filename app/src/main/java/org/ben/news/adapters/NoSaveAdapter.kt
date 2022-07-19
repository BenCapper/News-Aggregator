package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.databinding.CardAdBinding
import org.ben.news.databinding.CardStoryBinding
import org.ben.news.databinding.CardStoryNosaveBinding
import org.ben.news.models.StoryModel

interface StoryNoSaveListener {
    fun onStoryClick(story: StoryModel)
    fun onShare(story: StoryModel)
}

class NoSaveAdapter constructor(private var stories: ArrayList<StoryModel>, private val listener: StoryNoSaveListener, )
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var storage = FirebaseStorage.getInstance().reference


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType != 1){
            val binding = CardStoryNosaveBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            MainHolder(binding)

        } else {
            val binding = CardAdBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            StoryAdapter.AdHolder(binding)

        }
    }

    fun removeAt(position: Int) {
        stories.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MainHolder){
            val story = stories[holder.absoluteAdapterPosition]
            holder.bind(story, listener)
        }
        if (holder is StoryAdapter.AdHolder) {
            holder.bind()
        }

    }

    override fun getItemCount(): Int = stories.size

    override fun getItemViewType(position: Int): Int {
        return position % 5
    }

    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStoryNosaveBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener: StoryNoSaveListener) {
            var imgRef = storage.child(story.img_name)

            Glide.with(this.itemView.context).load(story.storage_link).into(binding.imageViewNosave)

            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.button3Nosave.setOnClickListener { listener.onShare(story) }
            binding.root.tag = story
            binding.story = story
            binding.executePendingBindings()
        }

    }


}