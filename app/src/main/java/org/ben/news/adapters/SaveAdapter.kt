package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import org.ben.news.R
import org.ben.news.databinding.CardAdBinding
import org.ben.news.databinding.CardStorySaveBinding
import org.ben.news.models.StoryModel
import timber.log.Timber
import kotlin.math.roundToInt


interface StorySaveListener {
    fun onStoryClick(story: StoryModel)
    fun onShare(story: StoryModel)
}

class SaveAdapter constructor(private var stories: ArrayList<StoryModel>, private val listener: StorySaveListener, )
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var ITEM = 0
    private var AD = 1
    private var FEED = 5
    private val ie = listOf("www.RTE.ie", "www.Gript.ie")
    private val uk = listOf("www.GBNews.uk", "news.Sky.com", "www.Spiked-Online.com", "www.TheGuardian.com", "www.DailyMail.co.uk", "www.DailySceptic.org",)
    private val ca = listOf("www.ThePostMillennial.com", "www.GlobalNews.ca")
    private val eu = listOf("www.Euronews.com")
    private val us = listOf("www.TheBlaze.com",
        "www.Timcast.com",
        "www.Revolver.news",
        "www.BonginoReport.com",
        "www.Zerohedge.com",
        "www.Breitbart.com",
        "www.Revolver.news",
        "www.DailyCaller.com",
        "www.InfoWars.com",
        "www.AmericanThinker.com",
        "www.TheDailyBeast.com",
        "www.TheGatewayPundit.com",
        "TrendingPoliticsNews.com",
        "www.Politico.com",
        "www.CbsNews.com",
        "AbcNews.go.com",
        "news.Yahoo.com",
        "www.Vox.com",
        "www.HuffPost.com",
        "www.Npr.org",
        "www.TheHill.com")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM){
            val binding = CardStorySaveBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            MainHolder(binding)

        } else {
            val binding = CardAdBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

            AdHolder(binding)

        }
    }

    fun removeAt(position: Int) {
        if (stories.size > 4) {
            val pos = position - position / FEED
            Timber.i("Position = $position")
            Timber.i("Pos = $pos")
            Timber.i("PosFeed = $FEED")
            Timber.i("PosSize = ${stories.size}")
            stories.removeAt(pos)
            notifyItemRemoved(position)
        }
        else{
            stories.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MainHolder){
            val pos = position - position / FEED
            val story = stories[pos]
            holder.bind(story, listener)
        }
        if (holder is AdHolder) {
            holder.bind()
        }

    }

    override fun getItemCount(): Int {
        if (stories.size > 0){
            return (stories.size + (stories.size / FEED).toDouble().roundToInt())
        }
        return stories.size
    }

    override fun getItemViewType(position: Int): Int {
        if ((position + 1) % FEED == 0) {
            return AD
        }
        return ITEM
    }

    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardStorySaveBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(story: StoryModel, listener: StorySaveListener) {


            if (story.outlet in ca){
                Glide.with(this.itemView.context).load(R.drawable.canicon).into(binding.imageView3)
            }
            if (story.outlet in eu){
                Glide.with(this.itemView.context).load(R.drawable.euicon).into(binding.imageView3)
            }
            if (story.outlet in uk){
                Glide.with(this.itemView.context).load(R.drawable.gbicon).into(binding.imageView3)
            }
            if (story.outlet in us){
                Glide.with(this.itemView.context).load(R.drawable.usicon).into(binding.imageView3)
            }
            if (story.outlet in ie){
                Glide.with(this.itemView.context).load(R.drawable.ieicon).into(binding.imageView3)
            }
            binding.root.setOnClickListener { listener.onStoryClick(story) }
            binding.button3Nosave.setOnClickListener { listener.onShare(story) }
            binding.root.tag = story
            binding.story = story
            binding.executePendingBindings()
        }

    }

    inner class AdHolder(private var binding : CardAdBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind() {

            val adLoader = AdLoader.Builder(this.itemView.context, "") //
                .forNativeAd { ad : NativeAd ->
                    populateNativeAdView(ad,binding)

                }
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, binding: CardAdBinding) {
        val nativeAdView = binding.root

        // Set the media view.
        nativeAdView.mediaView = binding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = binding.headad
        nativeAdView.bodyView = binding.body
        nativeAdView.callToActionView = binding.call
        nativeAdView.advertiserView = binding.advert

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.headad.text = nativeAd.headline
        binding.img.setImageDrawable(nativeAd.mediaContent!!.mainImage)
        nativeAd.mediaContent?.let { binding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            binding.body.visibility = View.INVISIBLE
        } else {
            binding.body.visibility = View.VISIBLE
            binding.body.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            binding.call.visibility = View.INVISIBLE
        } else {
            binding.call.visibility = View.VISIBLE
            binding.call.text = nativeAd.callToAction
        }


        if (nativeAd.advertiser == null) {
            binding.advert.visibility = View.INVISIBLE
        } else {
            binding.advert.text = nativeAd.advertiser
            binding.advert.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)
    }
}