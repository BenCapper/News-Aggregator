package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.ben.news.R
import org.ben.news.databinding.CardEmptyBinding
import org.ben.news.models.StoryModel


class EmptyAdapter constructor(
    private var stories: ArrayList<StoryModel>,
    private val listener: StoryListener
)
    : RecyclerView.Adapter<EmptyAdapter.MainHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardEmptyBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {

        val story = stories[holder.absoluteAdapterPosition]
        holder.bind(story, listener)

    }

    override fun getItemCount(): Int = stories.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardEmptyBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener : StoryListener) {


            binding.root.tag = story
            Glide.with(this.itemView.context).load(R.drawable.bidenfall).into(binding.imageView2)
            binding.executePendingBindings()
        }

    }

}
