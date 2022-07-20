package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.storage.FirebaseStorage
import org.ben.news.databinding.CardAdBinding
import org.ben.news.databinding.CardStoryBinding
import org.ben.news.databinding.CardStoryNosaveBinding
import org.ben.news.models.StoryModel
import timber.log.Timber

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

            AdNoSaveHolder(binding)

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
        if (holder is AdNoSaveHolder) {
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



            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.button3Nosave.setOnClickListener { listener.onShare(story) }
            binding.root.tag = story
            binding.story = story
            Glide.with(this.itemView.context).load(story.storage_link).into(binding.imageViewNosave)
            binding.executePendingBindings()
        }

    }

    inner class AdNoSaveHolder(private var binding : CardAdBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val nativeAdView = binding.root
            nativeAdView.mediaView = binding.adMedia
            nativeAdView.bodyView = binding.body
            nativeAdView.headlineView = binding.headad
            nativeAdView.callToActionView = binding.call

            val adLoader = AdLoader.Builder(this.itemView.context, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd { ad : NativeAd ->

                    ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }
                    binding.headad.text = ad.headline
                    binding.body.text = ad.body
                    binding.call.text = ad.callToAction
                    binding.img.setImageDrawable(ad.mediaContent!!.mainImage)
                    binding.nativeAd.setNativeAd(ad)


                }
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

    }


}