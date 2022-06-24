package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ben.news.databinding.CardStoryBinding
import org.ben.news.models.StoryModel
import java.util.*



class StoryAdapter constructor(private var stories: ArrayList<StoryModel>)
    : RecyclerView.Adapter<StoryAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardStoryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    fun removeAt(position: Int) {
        stories.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val story = stories[holder.absoluteAdapterPosition]
        holder.bind(story)
    }

    override fun getItemCount(): Int = stories.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStoryBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel) {
            binding.root.tag = story
            binding.story = story
            binding.executePendingBindings()
        }
    }
}
