package org.ben.news.ui.timcast

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.ben.news.R

class TimcastFragment : Fragment() {

    companion object {
        fun newInstance() = TimcastFragment()
    }

    private lateinit var viewModel: TimcastViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timcast, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TimcastViewModel::class.java)
        // TODO: Use the ViewModel
    }

}