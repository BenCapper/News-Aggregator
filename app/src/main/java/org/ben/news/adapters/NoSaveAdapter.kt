package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.databinding.CardStoryNosaveBinding
import org.ben.news.models.StoryModel

interface StoryNoSaveListener {
    fun onStoryClick(story: StoryModel)
}

class NoSaveAdapter constructor(private var stories: ArrayList<StoryModel>, private val listener: StoryNoSaveListener, )
    : RecyclerView.Adapter<NoSaveAdapter.MainHolder>() {

    private var storage = FirebaseStorage.getInstance().reference


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardStoryNosaveBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    fun removeAt(position: Int) {
        stories.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val story = stories[holder.absoluteAdapterPosition]
        holder.bind(story, listener)
    }

    override fun getItemCount(): Int = stories.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStoryNosaveBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener: StoryNoSaveListener) {
            var imgRef = storage.child(story.img_name)

            Glide.with(this.itemView.context).load(story.storage_link).override(1254,460).centerCrop().into(binding.imageView2Nosave)

            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.root.tag = story
            binding.story = story
            binding.executePendingBindings()
        }

    }


}