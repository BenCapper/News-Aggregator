package org.ben.news.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.ben.news.R
import org.ben.news.databinding.CardOutletBinding
import org.ben.news.models.OutletModel
import org.ben.news.models.StoryModel

interface OutletListener {
    fun onRadio(outlet: OutletModel)
}

class OutletAdapter constructor(
    private var outlets: ArrayList<OutletModel>,
    private val listener: OutletListener
)
    : RecyclerView.Adapter<OutletAdapter.MainHolder>() {

    private val right = listOf("Gript","GB News","Spiked Online","The Post Millennial","The Blaze",
        "Timcast","Revolver News","Bongino Report","Zerohedge","Breitbart",
        "The Daily Caller", "The Gateway Pundit", "American Thinker", "InfoWars", "The Daily Sceptic", "Trending Politics")
    private val left = listOf("RTE News","Sky News","The Guardian", "Global News","Euronews","The Daily Beast",
        "Politico", "CBS News",  "ABC News", "Yahoo News", "Vox","Huffington Post", "Daily Mail", "NPR", "The Hill")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardOutletBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {

        val outlet = outlets[holder.absoluteAdapterPosition]
        holder.bind(outlet, listener)

    }

    override fun getItemCount(): Int = outlets.size


    /* This class binds the building information to the recyclerview card */
    inner class MainHolder(private val binding : CardOutletBinding) : RecyclerView.ViewHolder(binding.root) {

        //val readOnlyRow = readOnly

        fun bind(outlet: OutletModel, listener : OutletListener) {

            if (outlet.region ==  "ca"){
                Glide.with(this.itemView.context).load(R.drawable.canicon).into(binding.imageViewOut)
            }
            if (outlet.region ==  "eu"){
                Glide.with(this.itemView.context).load(R.drawable.euicon).into(binding.imageViewOut)
            }
            if (outlet.region ==  "uk"){
                Glide.with(this.itemView.context).load(R.drawable.gbicon).into(binding.imageViewOut)
            }
            if (outlet.region ==  "us"){
                Glide.with(this.itemView.context).load(R.drawable.usicon).into(binding.imageViewOut)
            }
            if (outlet.region ==  "ie"){
                Glide.with(this.itemView.context).load(R.drawable.ieicon).into(binding.imageViewOut)
            }
            if (outlet.name in left){
                binding.divider3.setBackgroundColor(Color.parseColor("#2FB4FF"))
                binding.divider6.setBackgroundColor(Color.parseColor("#2FB4FF"))
            }
            if (outlet.name in right){
                binding.divider3.setBackgroundColor(Color.parseColor("#FF0000"))
                binding.divider6.setBackgroundColor(Color.parseColor("#FF0000"))
            }
            binding.radioButton.setOnClickListener { listener.onRadio(outlet) }
            binding.radioButton.isChecked =  outlet.selected
            binding.outlet = outlet
            binding.root.tag = outlet
            binding.executePendingBindings()
        }

    }

}