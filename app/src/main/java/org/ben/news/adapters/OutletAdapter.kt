package org.ben.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ben.news.databinding.CardOutletBinding
import org.ben.news.models.OutletModel

interface OutletListener {

}

class OutletAdapter constructor(
    private var outlets: ArrayList<OutletModel>,
    private val listener: OutletListener
)
    : RecyclerView.Adapter<OutletAdapter.MainHolder>() {



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

            binding.outlet = outlet
            binding.root.tag = outlet
            binding.executePendingBindings()
        }

    }

}