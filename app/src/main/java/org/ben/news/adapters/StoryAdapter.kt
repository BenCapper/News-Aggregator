package org.ben.news.adapters


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.ben.news.databinding.CardAdBinding
import org.ben.news.databinding.CardStoryBinding
import org.ben.news.models.StoryModel
import timber.log.Timber


interface StoryListener {
    fun onStoryClick(story: StoryModel)
    fun onLike(story: StoryModel)
    fun onShare(story: StoryModel)
}

class StoryAdapter constructor(
    private var stories: ArrayList<StoryModel>,
    private val listener: StoryListener
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType != 1){
            val binding = CardStoryBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            MainHolder(binding)

        } else {
            val binding = CardAdBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            AdHolder(binding)

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
        if (holder is AdHolder) {
            holder.bind()
        }

    }

    override fun getItemCount(): Int = stories.size


    override fun getItemViewType(position: Int): Int {
        return position % 5
    }
    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStoryBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener : StoryListener) {


            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.button.setOnClickListener { listener.onLike(story) }
            binding.button3.setOnClickListener { listener.onShare(story) }
            binding.root.tag = story
            binding.story = story
            Glide.with(this.itemView.context).load(story.storage_link).into(binding.imageView2)
            binding.executePendingBindings()
        }

    }


    inner class AdHolder(private var binding : CardAdBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind() {
            val adLoader = AdLoader.Builder(this.itemView.context, "ca-app-pub-3940256099942544/2247696110")
                .forNativeAd { ad : NativeAd ->
                    binding.headlinead.text = ad.body
                    binding.date.text = ad.headline
                    binding.linkad.text = ad.callToAction
                    binding.linkad2.text = ad.advertiser
                    binding.img.setImageDrawable(ad.mediaContent!!.mainImage)
                    Timber.i("IM DOING SOMETHING ${ad.mediaContent}")

                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

    }


}
