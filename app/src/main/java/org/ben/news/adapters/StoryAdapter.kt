package org.ben.news.adapters

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.bitmap.ResourceBitmapDecoder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import org.ben.news.R
import org.ben.news.databinding.CardStoryBinding
import org.ben.news.firebase.FirebaseImageManager
import org.ben.news.models.StoryModel
import splitties.views.imageBitmap
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface StoryListener {
    fun onStoryClick(story: StoryModel)
    fun onLike(story: StoryModel)
    fun onShare(story: StoryModel)
}

class StoryAdapter constructor(private var stories: ArrayList<StoryModel>, private val listener: StoryListener, )
    : RecyclerView.Adapter<StoryAdapter.MainHolder>() {

    private var storage = FirebaseStorage.getInstance().reference


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
        holder.bind(story, listener)
    }

    override fun getItemCount(): Int = stories.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStoryBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener : StoryListener) {

            Glide.with(this.itemView.context).load(story.storage_link).override(850,280).centerCrop().into(binding.imageView2)

            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.button.setOnClickListener { listener.onLike(story) }
            binding.button3.setOnClickListener { listener.onShare(story) }
            binding.root.tag = story
            binding.story = story
            binding.executePendingBindings()
        }

    }

}
