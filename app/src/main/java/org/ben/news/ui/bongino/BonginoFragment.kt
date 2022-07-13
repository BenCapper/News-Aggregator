package org.ben.news.ui.bongino

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ben.news.R

class BonginoFragment : Fragment() {

    companion object {
        fun newInstance() = BonginoFragment()
    }

    private lateinit var viewModel: BonginoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bongino, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BonginoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}